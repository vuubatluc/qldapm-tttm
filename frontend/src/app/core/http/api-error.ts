import { HttpErrorResponse } from '@angular/common/http';

/** Định dạng lỗi thống nhất từ backend: { code, message, details? }. */
export interface ApiError {
  code: string;
  message: string;
  details?: { field: string; message: string }[];
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

/** Lấy thông báo hiển thị cho người dùng từ một lỗi HTTP bất kỳ. */
export function toErrorMessage(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 0) {
      return 'Không kết nối được máy chủ. Vui lòng thử lại sau.';
    }
    const body = err.error as Partial<ApiError> | null;
    if (body?.details?.length) {
      return body.details.map((d) => `${d.field}: ${d.message}`).join('; ');
    }
    if (body?.message) {
      return body.message;
    }
  }
  return 'Đã có lỗi xảy ra. Vui lòng thử lại.';
}
