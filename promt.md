Bạn là Senior Java Backend Developer với 10+ năm kinh nghiệm về Spring Boot 3, Spring Security 6, JWT, Refresh Token và MySQL.

Dựa trên SRS của dự án Course Management & Project Grading System, hãy triển khai đầy đủ các chức năng xác thực sau:

* FR-01 Đăng nhập hệ thống (Cấp phát JWT)
* FR-02 Xoay vòng Token (Refresh Token Rotation)
* FR-03 Đăng xuất (Revoke Token)

=================================================

YÊU CẦU CHUNG

Công nghệ:

* Java 17
* Spring Boot 3.x
* Spring Security 6
* Spring Data JPA
* MySQL
* Lombok
* JWT (jjwt)

Kiến trúc:

controller
service
service.impl
repository
entity
dto.request
dto.response
security
exception
config

Quy tắc:

* Stateless Authentication
* JWT Access Token
* Refresh Token lưu Database
* Token Blacklist lưu Database
* Không sử dụng Enum
* role lưu String:

  * ADMIN
  * STUDENT
  * LECTURER

=================================================

FR-01 ĐĂNG NHẬP HỆ THỐNG (CẤP PHÁT JWT)

Mô tả:

Người dùng đăng nhập bằng email và password.

API:

POST /api/v1/auth/login

Request:

{
"email": "[student@gmail.com](mailto:student@gmail.com)",
"password": "Password@123"
}

Business Rules:

* Email phải tồn tại.
* Password phải đúng.
* User phải ở trạng thái ACTIVE.
* Password được kiểm tra bằng BCryptPasswordEncoder.

Khi đăng nhập thành công:

Sinh:

* Access Token
* Refresh Token

Lưu Refresh Token xuống database.

Response:

{
"accessToken": "...",
"refreshToken": "...",
"tokenType": "Bearer",
"expiresIn": 3600
}

Lỗi:

401 Unauthorized

=================================================

FR-02 XOAY VÒNG TOKEN (REFRESH TOKEN ROTATION)

Mô tả:

Khi Access Token hết hạn, client sử dụng Refresh Token để lấy Access Token mới.

API:

POST /api/v1/auth/refresh-token

Request:

{
"refreshToken": "xxxxxxxx"
}

Business Rules:

Kiểm tra:

* Refresh Token tồn tại.
* Chưa hết hạn.
* revoked = false.

Nếu hợp lệ:

1. Thu hồi Refresh Token cũ.
2. Sinh Refresh Token mới.
3. Sinh Access Token mới.
4. Lưu Refresh Token mới xuống DB.

Refresh Token Rotation bắt buộc.

Response:

{
"accessToken": "...",
"refreshToken": "...",
"tokenType": "Bearer",
"expiresIn": 3600
}

Lỗi:

401 Unauthorized

=================================================

FR-03 ĐĂNG XUẤT (REVOKE TOKEN)

Mô tả:

Người dùng đăng xuất khỏi hệ thống.

API:

POST /api/v1/auth/logout

Header:

Authorization: Bearer <access_token>

Business Rules:

1. Lấy Access Token từ Header.
2. Kiểm tra token hợp lệ.
3. Lưu Access Token vào TokenBlacklist.
4. Revoke tất cả Refresh Token còn hiệu lực của user.
5. Xóa SecurityContext.

Response:

{
"message": "Logout successfully"
}

Lỗi:

401 Unauthorized

=================================================

ENTITY

User

* id
* fullName
* email
* password
* phoneNumber
* role
* status
* createdAt
* updatedAt

RefreshToken

* id
* token
* expiredAt
* revoked
* createdAt

ManyToOne User

TokenBlacklist

* id
* token
* expiredAt
* createdAt

=================================================

SECURITY

Public APIs

/auth/login
/auth/register
/auth/refresh-token

ADMIN

/api/v1/admin/**

STUDENT

/api/v1/student/**

LECTURER

/api/v1/lecturer/**

=================================================

YÊU CẦU SINH CODE

Hãy tạo đầy đủ:

1. LoginRequest
2. LoginResponse
3. RefreshTokenRequest
4. RefreshTokenResponse
5. AuthenticationController
6. AuthenticationService
7. AuthenticationServiceImpl
8. RefreshTokenService
9. LogoutService
10. JwtService
11. JwtAuthenticationFilter
12. SecurityConfig
13. UserDetailsService
14. RefreshTokenRepository
15. TokenBlacklistRepository
16. Exception Handling
17. AuthenticationEntryPoint
18. AccessDeniedHandler

=================================================

YÊU CẦU BỔ SUNG

* Sử dụng Spring Security 6 chuẩn mới.
* Không dùng WebSecurityConfigurerAdapter.
* Sử dụng SecurityFilterChain.
* Dùng BCryptPasswordEncoder.
* Dùng AuthenticationManager.
* Kiểm tra TokenBlacklist trong JwtAuthenticationFilter.
* Giải thích luồng:
  Login → Access Token → Refresh Token → Refresh Rotation → Logout → Blacklist.
* Viết thêm Postman Collection cho 3 chức năng trên.

