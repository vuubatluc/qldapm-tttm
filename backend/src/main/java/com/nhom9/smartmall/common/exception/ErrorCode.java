package com.nhom9.smartmall.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/** Danh mục mã lỗi nghiệp vụ. FE dựa vào {@code code} để hiển thị thông báo. */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Chưa đăng nhập hoặc phiên đã hết hạn"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Tên đăng nhập hoặc mật khẩu không chính xác. Vui lòng kiểm tra lại."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "Phiên đăng nhập không hợp lệ, vui lòng đăng nhập lại"),
    ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "Tài khoản của bạn đã bị khóa tạm thời 15 phút do nhập sai mật khẩu quá 5 lần."),
    ACCOUNT_PENDING(HttpStatus.FORBIDDEN, "Tài khoản đang chờ Ban quản lý phê duyệt."),
    ACCOUNT_SUSPENDED(HttpStatus.FORBIDDEN, "Tài khoản của bạn đã bị tạm dừng hoạt động. Vui lòng liên hệ Ban quản lý TTTM."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy dữ liệu"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "Email đã được sử dụng"),
    TENANT_ID_DUPLICATED(HttpStatus.CONFLICT, "Mã số thuế/CCCD đã tồn tại ở khách thuê khác"),
    TENANT_ACCOUNT_EXISTS(HttpStatus.CONFLICT, "Khách thuê đã có tài khoản cổng thông tin"),
    USER_ALREADY_LINKED(HttpStatus.CONFLICT, "Tài khoản đã được liên kết với hồ sơ khách thuê khác"),
    USER_NOT_TENANT_ROLE(HttpStatus.CONFLICT, "Chỉ tài khoản vai trò Khách thuê mới liên kết được với hồ sơ khách thuê"),
    UNIT_NOT_AVAILABLE(HttpStatus.CONFLICT, "Gian hàng không ở trạng thái Trống"),
    CONTRACT_PERIOD_OVERLAP(HttpStatus.CONFLICT, "Kỳ thuê bị trùng với hợp đồng khác của gian hàng"),
    CONTRACT_INVALID_STATE(HttpStatus.CONFLICT, "Trạng thái hợp đồng không cho phép thao tác này"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Hệ thống đang bận, vui lòng thử lại sau ít phút");

    private final HttpStatus status;
    private final String defaultMessage;
}
