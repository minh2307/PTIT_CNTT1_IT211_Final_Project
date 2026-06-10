Bạn là Senior Java Backend Developer với 10+ năm kinh nghiệm về Spring Boot, Spring Security, JPA/Hibernate, MySQL.

Hãy triển khai các chức năng sau theo đúng tài liệu SRS của dự án Course Management and Project Grading System.

## Yêu cầu chung

### Công nghệ

* Java 17
* Spring Boot 3.x
* Spring Data JPA
* Spring Security + JWT
* MySQL
* Lombok
* Validation
* MapStruct (nếu cần)
* Maven

### Kiến trúc dự án

Áp dụng kiến trúc:

controller
service
service.impl
repository
entity
dto.request
dto.response
security
exception
mapper
config

### Quy tắc code

* Tuân thủ RESTful API
* Sử dụng ResponseEntity
* Validation bằng Bean Validation
* Xử lý exception bằng @RestControllerAdvice
* Soft Delete nếu phù hợp
* Phân trang bằng Pageable
* API trả về ApiResponse<T>
* Viết đầy đủ:

  * Entity
  * DTO Request
  * DTO Response
  * Repository
  * Service
  * ServiceImpl
  * Controller
  * Mapper
  * Exception

---

# FR-04 Đăng ký tài khoản Sinh viên mới

## Mô tả nghiệp vụ

Sinh viên có thể tự đăng ký tài khoản.

Thông tin đăng ký:

* fullName
* email
* password
* confirmPassword
* phoneNumber

## Quy tắc nghiệp vụ

* Email không được trùng.
* Email đúng định dạng.
* Password tối thiểu 8 ký tự.
* Password phải chứa:

  * chữ hoa
  * chữ thường
  * số
* confirmPassword phải khớp password.
* Role mặc định là STUDENT.
* Status mặc định là ACTIVE.
* Mật khẩu phải mã hóa bằng BCryptPasswordEncoder.

## API

POST /api/v1/auth/register

Request:

{
"fullName": "Nguyen Van A",
"email": "[a@gmail.com](mailto:a@gmail.com)",
"password": "Password@123",
"confirmPassword": "Password@123",
"phoneNumber": "0123456789"
}

Response:

{
"code": 201,
"message": "Register success"
}

---

# FR-05 Quản lý Người dùng & Lớp học

Chỉ ADMIN được phép sử dụng.

## User Management

### API

1. Tạo User

POST /api/v1/admin/users

2. Danh sách User

GET /api/v1/admin/users

Yêu cầu:

* Pagination
* Sorting
* Search theo:

  * fullName
  * email

Ví dụ:

GET /api/v1/admin/users?page=0&size=10&keyword=minh

3. Chi tiết User

GET /api/v1/admin/users/{id}

4. Cập nhật User

PUT /api/v1/admin/users/{id}

5. Xóa User

DELETE /api/v1/admin/users/{id}

---

## Class Management

Entity ClassRoom

Thuộc tính:

* id
* classCode
* className
* description
* createdAt
* updatedAt

### API

POST /api/v1/admin/classes

GET /api/v1/admin/classes

GET /api/v1/admin/classes/{id}

PUT /api/v1/admin/classes/{id}

DELETE /api/v1/admin/classes/{id}

Yêu cầu:

* Pagination
* Search theo:

  * classCode
  * className

---

# FR-06 Đăng ký tham gia khóa học

Sinh viên đăng ký tham gia khóa học.

## Entity

Course

* id
* courseCode
* courseName
* description
* maxStudents
* status

Enrollment

* id
* student
* course
* enrolledAt
* status

## Quy tắc nghiệp vụ

* Chỉ STUDENT được đăng ký.
* Không được đăng ký trùng khóa học.
* Khóa học phải tồn tại.
* Khóa học phải ở trạng thái OPEN.
* Số lượng học viên không vượt quá maxStudents.

## API

POST /api/v1/student/courses/{courseId}/enroll

Response:

{
"code": 200,
"message": "Enroll successfully"
}

## API xem danh sách khóa học đã đăng ký

GET /api/v1/student/enrollments

Có phân trang.

---

# Yêu cầu bổ sung

1. Thiết kế ERD.
2. Thiết kế Entity Relationship.
3. Viết SQL tạo database MySQL.
4. Viết đầy đủ source code theo từng phần.
5. Viết Postman Collection.
6. Viết Swagger OpenAPI.
7. Viết Security Config phân quyền:

ADMIN:

* Quản lý User
* Quản lý Class

STUDENT:

* Đăng ký khóa học
* Xem khóa học đã đăng ký

8. Áp dụng JWT Authentication.

9. Viết unit test cho Service Layer.

10. Giải thích ngắn gọn luồng xử lý của từng API.
