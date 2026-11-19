# Coding Convention — Smart Mall (Nhóm 9)

Tài liệu áp dụng cho toàn bộ mã nguồn trong repo. Mục tiêu: 6 người viết code mà đọc như một người viết.
Khi có xung đột với thói quen cá nhân, **làm theo tài liệu này**. Muốn đổi quy ước → mở PR sửa file này trước.

| Thành phần | Công nghệ chốt |
|---|---|
| Frontend | Angular 22 (standalone component, signals, Reactive Forms), TypeScript strict, SCSS |
| Backend | Java 21, Spring Boot 4.1, Spring Security (OAuth2 Resource Server – JWT HS256), Spring Data JPA |
| CSDL | PostgreSQL 16, migration bằng Flyway |
| API | REST, tiền tố `/api/v1`, mô tả bằng OpenAPI (springdoc) tại `/swagger-ui.html` |

---

## 1. Quy ước chung

- **Ngôn ngữ**: tên biến/hàm/lớp/bảng/cột bằng **tiếng Anh**. Comment, Javadoc, thông báo cho người dùng bằng **tiếng Việt có dấu**.
- **Mã hóa**: UTF-8, xuống dòng LF, thụt lề bằng dấu cách (Java 4, TS/HTML/SCSS/SQL/YAML 2). Đã cấu hình trong `.editorconfig`.
- **Độ dài dòng**: Java ≤ 120 ký tự, TS/HTML ≤ 100 ký tự (Prettier).
- **Không commit**: mật khẩu, JWT secret, file `.env`, thư mục `node_modules/`, `target/`, `dist/`.
- **Thuật ngữ nghiệp vụ** dùng thống nhất giữa FE – BE – CSDL:

| Nghiệp vụ | Tên trong code | Ghi chú |
|---|---|---|
| Ban quản lý / Kế toán / Kỹ thuật viên / Khách thuê | `ADMIN` / `ACCOUNTANT` / `TECHNICIAN` / `TENANT` | Enum `Role` |
| Gian hàng | `unit` (`unitCode` ví dụ `F1-15`) | Không dùng `booth`, `stall`, `shop` |
| Tầng / Phân khu | `floor` / `zone` | |
| Khách thuê | `tenant` (`tenantCode` ví dụ `KT-0012`) | |
| Hợp đồng / Dòng gian trong hợp đồng | `contract` / `contractUnit` | |
| Kỳ thuê | `leaseFrom`, `leaseToExclusive` | Khoảng nửa mở `[from, toExclusive)` |

## 2. Git & quy trình làm việc

- Nhánh chính: `main` (luôn build được). Không push thẳng vào `main`.
- Tên nhánh: `<loại>/<mã-việc>-<mô-tả-ngắn>`, ví dụ `feature/KT01-tenant-list`, `fix/TK01-lockout`.
  Loại: `feature`, `fix`, `refactor`, `docs`, `chore`, `test`.
- Commit theo **Conventional Commits**, mô tả tiếng Việt hoặc Anh đều được nhưng phải rõ:
  ```
  feat(tenant): thêm API tìm kiếm khách thuê theo MST
  fix(auth): khóa tài khoản sau 5 lần sai mật khẩu
  ```
  Scope là tên module: `auth`, `user`, `tenant`, `contract`, `premises`, `fe-core`, `db`...
- Mỗi Pull Request: một đầu việc, ≤ ~400 dòng thay đổi, có ít nhất **1 người review** đồng ý mới merge.
  Trước khi mở PR phải chạy được: `./mvnw test` (BE) và `npm run build && npm test -- --watch=false` (FE).
- Mã use case / yêu cầu (UC-TK01, UC-KT-LEASING, FR-TK-02…) ghi trong mô tả PR để truy vết với tài liệu phân tích.

## 3. Backend (Spring Boot)

### 3.1 Cấu trúc package — chia theo module nghiệp vụ

```
com.nhom9.smartmall
├── common/            # dùng chung: BaseEntity, exception, response, config
├── security/          # SecurityConfig, JWT, CurrentUser
├── auth/              # đăng nhập, đăng ký, tài khoản (UC-TK01)
│   ├── controller/  dto/  entity/  repository/  service/
├── user/              # quản trị tài khoản & phân quyền (BQL)
├── premises/          # tầng, phân khu, gian hàng
├── tenant/            # hồ sơ khách thuê
└── contract/          # hợp đồng, gán gian hàng
```

- Module mới (ví dụ `billing`, `maintenance` của Module 2) tạo package cùng cấp, giữ đủ 5 package con.
- Module A cần dữ liệu của module B → gọi **service** của B. Chỉ dùng trực tiếp repository của module khác khi cần khóa dòng trong cùng giao dịch (ví dụ `UnitRepository.findByIdForUpdate` trong `ContractService`), và ghi comment lý do.
- Tránh phụ thuộc vòng giữa service: tách lớp truy vấn tổng hợp (ví dụ `TenantQueryService`).

### 3.2 Đặt tên

| Loại | Quy tắc | Ví dụ |
|---|---|---|
| Class | PascalCase, danh từ | `TenantService`, `ContractUnit` |
| Method / biến | camelCase, động từ cho method | `createDraft()`, `activeLeasesOf()` |
| Hằng số | UPPER_SNAKE_CASE | `MAX_FAILED_ATTEMPTS` |
| Controller | `<Tên>Controller` | `TenantController` |
| Service | `<Tên>Service` (ghi) / `<Tên>QueryService` (đọc tổng hợp) | |
| Repository | `<Entity>Repository` | `UnitRepository` |
| DTO | record, hậu tố `Request` / `Response` / `Summary` / `Detail` | `TenantRequest`, `TenantSummary` |
| Enum | PascalCase, giá trị UPPER_SNAKE_CASE | `UnitStatus.AVAILABLE` |

### 3.3 Phân tầng & trách nhiệm

- **Controller**: chỉ nhận request, `@Valid`, kiểm tra quyền, gọi service, trả DTO. **Không** chứa logic nghiệp vụ, **không** gọi repository.
- **Service**: toàn bộ logic nghiệp vụ, đặt `@Transactional` ở đây (`readOnly = true` cho truy vấn).
- **Repository**: Spring Data JPA. Truy vấn phức tạp viết JPQL trong `@Query` với text block `"""`.
- **Entity**: không bao giờ trả trực tiếp ra API. Luôn map sang DTO (static `from(...)` trong record DTO).
- Inject bằng constructor (`@RequiredArgsConstructor` + field `private final`). Không dùng `@Autowired` lên field.

### 3.4 Entity & Lombok

- Kế thừa `BaseEntity` (id UUID, `createdAt`, `updatedAt` tự động).
- Chỉ dùng `@Getter`, `@Setter`, `@NoArgsConstructor`, `@RequiredArgsConstructor`, `@Slf4j`.
  **Cấm** `@Data`, `@EqualsAndHashCode`, `@ToString` trên entity (gây lỗi lazy-load, vòng lặp vô hạn).
- Quan hệ `@ManyToOne` luôn `fetch = FetchType.LAZY`; tải kèm bằng `@EntityGraph` hoặc `join fetch` khi cần.
- Enum lưu bằng `@Enumerated(EnumType.STRING)`.
- Tiền: `BigDecimal` (`DECIMAL(18,2)`), diện tích `DECIMAL(10,2)`. **Không** dùng `double/float` cho tiền.
- Ngày nghiệp vụ: `LocalDate`; mốc thời gian thao tác: `Instant` (`TIMESTAMPTZ`, lưu UTC).

### 3.5 API REST

- URL: danh từ số nhiều, kebab-case: `/api/v1/tenants`, `/api/v1/tenants/{tenantId}/portal-account`.
- Hành động nghiệp vụ không phải CRUD dùng `POST` + động từ/danh từ: `POST /contracts/{id}/activate`, `POST /contracts/{id}/terminations`.
- JSON dùng camelCase. Ngày `YYYY-MM-DD`, thời điểm ISO-8601 UTC. Tiền đơn vị VND.
- Mã HTTP: `200` đọc/cập nhật, `201` tạo mới, `400` dữ liệu sai, `401` chưa đăng nhập, `403` không có quyền, `404` không tìm thấy, `409` xung đột (trùng lịch, trùng MST, sai trạng thái).
- Lỗi **luôn** có dạng:
  ```json
  { "code": "CONTRACT_PERIOD_OVERLAP", "message": "Kỳ thuê bị trùng...", "details": [ { "field": "...", "message": "..." } ] }
  ```
  Thêm lỗi mới → thêm giá trị vào `ErrorCode`, ném `BusinessException`. Không tự tạo `ResponseEntity` lỗi trong controller.
- Danh sách có phân trang trả `PageResponse<T>`; tham số `page` (từ 0), `size`, `sort`.
- Mỗi endpoint có `@Operation(summary = "...")` để Swagger hiển thị.

### 3.6 Bảo mật & phân quyền

- Mặc định mọi endpoint yêu cầu đăng nhập; endpoint công khai phải khai báo trong `SecurityConfig.PUBLIC_ENDPOINTS`.
- Phân quyền theo vai trò bằng `@PreAuthorize("hasRole('ADMIN')")` / `hasAnyRole(...)` **ở từng method controller** theo ma trận RBAC (tài liệu phân tích mục 1.2).
- Kiểm tra quyền **theo dữ liệu** (khách thuê chỉ xem hồ sơ của mình) bằng `CurrentUser.from(jwt)` và so `tenantId`.
- Mật khẩu băm BCrypt; không log mật khẩu, token, số CCCD.
- Thông báo lỗi đăng nhập không được tiết lộ email có tồn tại hay không.

### 3.7 CSDL & Flyway

- Mọi thay đổi schema qua file migration `backend/src/main/resources/db/migration/V<số>__<mô_tả>.sql`.
  **Không sửa** migration đã merge vào `main` — tạo file mới.
- `spring.jpa.hibernate.ddl-auto=validate` — Hibernate không tự tạo bảng.
- Tên bảng/cột: `snake_case`, bảng số ít (`tenant`, `contract_unit`). Khóa chính `id UUID`.
- Ràng buộc đặt tên: `pk_`, `fk_<bảng>_<cột>`, `uq_<bảng>_<cột>`, `ck_<bảng>_<ý nghĩa>`, chỉ mục `ix_<bảng>_<cột>`.
- Không xóa cứng dữ liệu đã được tham chiếu (khách thuê, hợp đồng): dùng cột trạng thái.
- Thao tác cạnh tranh (kích hoạt hợp đồng) phải khóa dòng theo thứ tự UUID trong cùng giao dịch.

### 3.8 Log & test

- Log qua `@Slf4j`: `info` cho sự kiện nghiệp vụ quan trọng, `warn` cho bất thường có thể xử lý, `error` kèm exception.
- Test đặt cùng package với lớp được test, tên `<Lớp>Test`, method mô tả hành vi: `fifthWrongPassword_locksAccountFor15Minutes`.
- Service có logic nghiệp vụ phải có unit test (JUnit 5 + Mockito + AssertJ).

## 4. Frontend (Angular)

### 4.1 Cấu trúc thư mục

```
src/app/
├── core/               # singleton dùng toàn app: auth (service, guard, interceptor), http
├── shared/             # component/pipe/validator dùng lại, không phụ thuộc feature
├── layout/             # khung giao diện (sidebar, topbar)
└── features/           # mỗi nghiệp vụ một thư mục, lazy-load
    ├── auth/           # login, register
    ├── tenants/        # danh sách, form, chi tiết, gán gian + tenant.api.ts + tenant.models.ts
    ├── users/          # duyệt tài khoản & phân quyền
    └── portal/         # cổng khách thuê
```

### 4.2 Đặt tên file & lớp

| Loại | File | Lớp |
|---|---|---|
| Trang (gắn route) | `tenant-list.page.ts/html` | `TenantListPage` |
| Component con | `assign-unit-form.component.ts` | `AssignUnitFormComponent` |
| Gọi API | `tenant.api.ts` | `TenantApi` |
| Service trạng thái | `auth.service.ts` | `AuthService` |
| Model | `tenant.models.ts` | `interface Tenant`, `type TenantStatus` |
| Guard / interceptor | `auth.guards.ts`, `auth.interceptor.ts` | `authGuard`, `authInterceptor` (hàm) |
| Route của feature | `tenants.routes.ts` | `TENANT_ROUTES` |
| Test | `*.spec.ts` cạnh file được test | |

File: kebab-case. Selector: tiền tố `app-`, ví dụ `app-tenant-list-page`.

### 4.3 Quy tắc viết component

- Chỉ dùng **standalone component**; không tạo `NgModule`.
- Inject bằng `inject()`, không inject qua constructor.
- Trạng thái component dùng **signal** (`signal`, `computed`); input/output dùng `input()`, `output()`.
- Template dùng control flow mới `@if`, `@for (...; track ...)`, `@let`; không dùng `*ngIf`, `*ngFor`.
- Form nhập liệu dùng **Reactive Forms** (`NonNullableFormBuilder`); regex kiểm tra đặt ở `shared/validation.ts` và phải khớp với backend.
- Thành viên dùng trong template khai báo `protected`; chỉ `public` khi component cha cần gọi.
- Template > ~15 dòng tách ra file `.html`. Style riêng của component tối đa ~4 kB; style dùng chung để trong `styles.scss` (biến màu `--sm-*`).
- Component **không** gọi `HttpClient` trực tiếp — gọi qua lớp `*.api.ts` của feature (ngoại lệ tạm: trang quản trị nhỏ chỉ có một màn hình).

### 4.4 HTTP, lỗi, xác thực

- URL API lấy từ `environment.apiUrl` (`/api/v1`); dev dùng `proxy.conf.json` chuyển về `localhost:8080`.
- Token được `authInterceptor` gắn tự động; 401 → tự refresh một lần rồi gửi lại, thất bại thì đăng xuất.
- Hiển thị lỗi bằng `toErrorMessage(err)` (đọc `message` từ backend), không tự viết lại thông báo.
- Mọi route nghiệp vụ phải có `authGuard` + `roleGuard(...)`. Guard chỉ là lớp giao diện — backend luôn kiểm tra quyền lại.

### 4.5 TypeScript

- `strict: true`, `strictTemplates: true`. Không dùng `any`; dùng `unknown` rồi thu hẹp kiểu.
- Kiểu dữ liệu API khai báo `interface` trong `*.models.ts`, khớp tên trường với DTO backend.
- Nhãn tiếng Việt cho enum đặt trong hằng `*_LABELS: Record<Enum, string>`.
- Format bằng Prettier (`npx prettier --write "src/**/*.{ts,html,scss}"`) trước khi commit.

## 5. Checklist trước khi mở PR

- [ ] Build & test BE: `cd backend && ./mvnw test`
- [ ] Build & test FE: `cd frontend && npm run build && npm test -- --watch=false`
- [ ] Thay đổi schema có file Flyway mới, không sửa file cũ
- [ ] Endpoint mới có `@PreAuthorize` đúng ma trận RBAC và `@Operation`
- [ ] Lỗi nghiệp vụ dùng `BusinessException` + `ErrorCode`
- [ ] Không có secret, `console.log`, `System.out.println`, code bị comment bỏ
- [ ] Mô tả PR ghi mã UC/FR liên quan
