package com.example.finalproject.mapper;

import com.example.finalproject.model.dto.response.CourseResponse;
import com.example.finalproject.model.entity.Course;

public class CourseMapper {

    public static CourseResponse toCourseResponse(Course course) {
        if (course == null) {
            return null;
        }
        return CourseResponse.builder()
                .id(course.getId())
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .description(course.getDescription())
                .maxStudents(course.getMaxStudents())
                .status(course.getStatus())
                .build();
    }
}
