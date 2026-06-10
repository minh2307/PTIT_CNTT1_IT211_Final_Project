package com.example.finalproject.service.impl;

import com.example.finalproject.exception.AppException;
import com.example.finalproject.mapper.LectureMaterialMapper;
import com.example.finalproject.model.dto.request.LectureMaterialRequest;
import com.example.finalproject.model.dto.response.LectureMaterialResponse;
import com.example.finalproject.model.entity.Course;
import com.example.finalproject.model.entity.LectureMaterial;
import com.example.finalproject.model.entity.User;
import com.example.finalproject.repository.CourseRepository;
import com.example.finalproject.repository.EnrollmentRepository;
import com.example.finalproject.repository.LectureMaterialRepository;
import com.example.finalproject.repository.UserRepository;
import com.example.finalproject.service.FileStorageService;
import com.example.finalproject.service.LectureMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LectureMaterialServiceImpl implements LectureMaterialService {

    private final LectureMaterialRepository lectureMaterialRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final FileStorageService fileStorageService;
    private final LectureMaterialMapper lectureMaterialMapper;

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of("pdf", "docx", "pptx", "zip");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB limit

    @Override
    @Transactional
    public LectureMaterialResponse uploadMaterial(Long courseId, LectureMaterialRequest request, String lecturerEmail) {
        // Find lecturer
        User lecturer = userRepository.findByEmail(lecturerEmail)
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "User not found"));

        // Rule: Only LECTURER can upload
        if (!"LECTURER".equalsIgnoreCase(lecturer.getRole())) {
            throw new AppException(HttpStatus.FORBIDDEN, "Only lecturers can upload course materials");
        }

        // Rule: Course must exist
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Course not found"));

        // File validation
        if (request.getFile() == null || request.getFile().isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "File is required");
        }

        // File size check
        if (request.getFile().getSize() > MAX_FILE_SIZE) {
            throw new AppException(HttpStatus.BAD_REQUEST, "File size exceeds limit of 10MB");
        }

        // Supported formats check
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(request.getFile().getOriginalFilename()));
        String extension = "";
        int i = originalFileName.lastIndexOf('.');
        if (i > 0) {
            extension = originalFileName.substring(i + 1).toLowerCase();
        }

        if (!SUPPORTED_EXTENSIONS.contains(extension)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Unsupported file format. Supported formats: pdf, docx, pptx, zip");
        }

        // Save file
        String fileUrl = fileStorageService.storeFile(request.getFile());

        LectureMaterial material = LectureMaterial.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .fileUrl(fileUrl)
                .fileType(extension)
                .status("ACTIVE")
                .course(course)
                .lecturer(lecturer)
                .build();

        LectureMaterial savedMaterial = lectureMaterialRepository.save(material);
        return lectureMaterialMapper.toLectureMaterialResponse(savedMaterial);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LectureMaterialResponse> getCourseMaterials(Long courseId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "User not found"));

        // Check if course exists
        if (!courseRepository.existsById(courseId)) {
            throw new AppException(HttpStatus.NOT_FOUND, "Course not found");
        }

        // Authorization check: "Public cho người đã tham gia khóa học"
        // Admin and Lecturer roles can see materials directly.
        // Students can see materials only if they are enrolled in this course.
        if ("STUDENT".equalsIgnoreCase(user.getRole())) {
            boolean isEnrolled = enrollmentRepository.existsByUserIdAndCourseId(user.getId(), courseId);
            if (!isEnrolled) {
                throw new AppException(HttpStatus.FORBIDDEN, "Access denied. You must be enrolled in this course to view materials.");
            }
        }

        List<LectureMaterial> materials = lectureMaterialRepository.findByCourseIdAndStatus(courseId, "ACTIVE");
        return materials.stream()
                .map(lectureMaterialMapper::toLectureMaterialResponse)
                .collect(Collectors.toList());
    }
}
