# Smart Mall — Hệ thống quản lý Trung tâm Thương mại thông minh (Nhóm 9)

Phạm vi hiện tại: **xác thực & phân quyền** (đăng nhập, đăng ký, 4 vai trò BQL / Kế toán / Kỹ thuật / Khách thuê)
và **quản lý khách thuê gian hàng** (hồ sơ khách thuê, gán gian qua hợp đồng, cấp tài khoản cổng khách thuê).

```
qldapm-tttm/
├── backend/            Spring Boot 4.1 · Java 21 · PostgreSQL · Flyway
├── frontend/           Angular 22
├── docs/               CODING_CONVENTION.md
└── docker-compose.yml  PostgreSQL 16 cho dev
```

Quy ước code: [docs/CODING_CONVENTION.md](docs/CODING_CONVENTION.md)

## Yêu cầu

- JDK 21, Node.js 22.22+ / 24.15+ / 26+, Docker (hoặc PostgreSQL 16 cài sẵn)

## Chạy bằng Docker (cả hệ thống)

```bash
cp .env.example .env   # rồi điền JWT_SECRET, DB_PASSWORD, DEV_SEED_PASSWORD
docker compose up -d --build
```

Frontend http://localhost:4200 · Swagger http://localhost:8080/swagger-ui.html

## Chạy dev (FE/BE ngoài Docker)

1. Khởi động CSDL:
   ```bash
   docker compose up -d postgres
   ```
2. Backend (cổng 8080, tự chạy Flyway migration và tạo tài khoản mẫu ở profile `dev`):
   ```bash
   cd backend && ./mvnw spring-boot:run
   ```
   Swagger: http://localhost:8080/swagger-ui.html
3. Frontend (cổng 4200, `/api` được proxy sang 8080):
   ```bash
   cd frontend && npm install && npm start
   ```
   Mở http://localhost:4200

### Tài khoản mẫu (profile dev)

Mật khẩu chung lấy từ `app.dev-seed.password` trong `backend/src/main/resources/application-dev.yml`
(có thể đổi bằng biến môi trường `DEV_SEED_PASSWORD`).

| Email | Vai trò |
|---|---|
| admin@smartmall.local | Ban quản lý |
| ketoan@smartmall.local | Kế toán |
| kythuat@smartmall.local | Kỹ thuật viên |

Tài khoản khách thuê: BQL tạo khi thêm khách thuê (tick "Cấp tài khoản"), hoặc khách thuê tự đăng ký rồi BQL duyệt ở mục *Tài khoản & phân quyền*.

## Biến môi trường (môi trường test/production)

| Biến | Ý nghĩa |
|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Kết nối PostgreSQL |
| `JWT_SECRET` | Khóa ký JWT, ≥ 32 ký tự — **bắt buộc** ngoài dev |
| `CORS_ALLOWED_ORIGINS` | Origin của frontend |
| `SPRING_PROFILES_ACTIVE` | Đặt khác `dev` để tắt dữ liệu mẫu |

## API chính

| Phương thức | Đường dẫn | Quyền |
|---|---|---|
| POST | `/api/v1/auth/login`, `/auth/register`, `/auth/refresh` | Công khai |
| GET | `/api/v1/auth/me` | Đã đăng nhập |
| GET/POST/PATCH | `/api/v1/users`, `/users/{id}/role`, `/users/{id}/status` | BQL |
| GET | `/api/v1/floors`, `/api/v1/units` | Đã đăng nhập |
| GET | `/api/v1/tenants`, `/tenants/{id}` | BQL, Kế toán (khách thuê: chỉ hồ sơ mình) |
| POST/PUT/PATCH | `/api/v1/tenants`, `/tenants/{id}`, `/tenants/{id}/status`, `/tenants/{id}/portal-account` | BQL |
| GET/PATCH | `/api/v1/tenants/me` | Khách thuê |
| POST | `/api/v1/contracts`, `/contracts/{id}/activate`, `/cancel`, `/terminations` | BQL |

## Chưa làm (để các sprint sau)

- Quên mật khẩu (OTP email), 2FA, refresh token trong HttpOnly cookie
- Upload hồ sơ pháp lý / PDF hợp đồng, sinh mã QR gian hàng, phụ lục & gia hạn hợp đồng
- Luồng thanh lý đầy đủ (xác nhận kế toán + kỹ thuật)
- Module 2: điện nước, hóa đơn, công nợ, bảo trì
