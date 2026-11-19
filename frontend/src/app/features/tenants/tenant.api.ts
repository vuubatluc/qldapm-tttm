import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PageResponse } from '../../core/http/api-error';
import {
  Contract,
  CreateContractRequest,
  Floor,
  PortalAccount,
  Tenant,
  TenantCreatedResponse,
  TenantDetail,
  TenantRequest,
  TenantStatus,
  TenantSummary,
  Unit,
  UnitStatus,
  UnlinkedTenantUser,
} from './tenant.models';

/** Gọi API khách thuê, hợp đồng và mặt bằng. Component không gọi HttpClient trực tiếp. */
@Injectable({ providedIn: 'root' })
export class TenantApi {
  private readonly http = inject(HttpClient);
  private readonly api = environment.apiUrl;

  search(q: { keyword?: string; status?: TenantStatus | ''; page: number; size: number }) {
    let params = new HttpParams().set('page', q.page).set('size', q.size);
    if (q.keyword) params = params.set('keyword', q.keyword);
    if (q.status) params = params.set('status', q.status);
    return this.http.get<PageResponse<TenantSummary>>(`${this.api}/tenants`, { params });
  }

  get(id: string): Observable<TenantDetail> {
    return this.http.get<TenantDetail>(`${this.api}/tenants/${id}`);
  }

  me(): Observable<TenantDetail> {
    return this.http.get<TenantDetail>(`${this.api}/tenants/me`);
  }

  create(req: TenantRequest): Observable<TenantCreatedResponse> {
    return this.http.post<TenantCreatedResponse>(`${this.api}/tenants`, req);
  }

  update(id: string, req: TenantRequest): Observable<Tenant> {
    return this.http.put<Tenant>(`${this.api}/tenants/${id}`, req);
  }

  changeStatus(id: string, status: TenantStatus): Observable<Tenant> {
    return this.http.patch<Tenant>(`${this.api}/tenants/${id}/status`, { status });
  }

  createPortalAccount(id: string): Observable<PortalAccount> {
    return this.http.post<PortalAccount>(`${this.api}/tenants/${id}/portal-account`, {});
  }

  /** Tài khoản Khách thuê tự đăng ký, chưa gắn hồ sơ nào. */
  unlinkedTenantUsers(): Observable<PageResponse<UnlinkedTenantUser>> {
    const params = new HttpParams()
      .set('role', 'TENANT')
      .set('unlinkedOnly', true)
      .set('size', 100)
      .set('sort', 'email');
    return this.http.get<PageResponse<UnlinkedTenantUser>>(`${this.api}/users`, { params });
  }

  linkAccount(id: string, userId: string): Observable<PortalAccount> {
    return this.http.post<PortalAccount>(`${this.api}/tenants/${id}/portal-account/link`, { userId });
  }

  unlinkAccount(id: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/tenants/${id}/portal-account`);
  }

  floors(): Observable<Floor[]> {
    return this.http.get<Floor[]>(`${this.api}/floors`);
  }

  units(q: { floorId?: string; status?: UnitStatus }): Observable<PageResponse<Unit>> {
    let params = new HttpParams().set('size', 200);
    if (q.floorId) params = params.set('floorId', q.floorId);
    if (q.status) params = params.set('status', q.status);
    return this.http.get<PageResponse<Unit>>(`${this.api}/units`, { params });
  }

  createContract(req: CreateContractRequest): Observable<Contract> {
    return this.http.post<Contract>(`${this.api}/contracts`, req);
  }

  activateContract(id: string): Observable<Contract> {
    return this.http.post<Contract>(`${this.api}/contracts/${id}/activate`, {});
  }

  cancelContract(id: string): Observable<Contract> {
    return this.http.post<Contract>(`${this.api}/contracts/${id}/cancel`, {});
  }

  terminateContract(id: string): Observable<Contract> {
    return this.http.post<Contract>(`${this.api}/contracts/${id}/terminations`, {});
  }
}
