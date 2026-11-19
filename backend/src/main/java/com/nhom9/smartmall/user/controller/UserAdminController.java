package com.nhom9.smartmall.user.controller;

import com.nhom9.smartmall.auth.entity.Role;
import com.nhom9.smartmall.auth.entity.UserStatus;
import com.nhom9.smartmall.common.response.PageResponse;
import com.nhom9.smartmall.security.CurrentUser;
import com.nhom9.smartmall.user.dto.UserAdminDtos.CreateUserRequest;
import com.nhom9.smartmall.user.dto.UserAdminDtos.UpdateRoleRequest;
import com.nhom9.smartmall.user.dto.UserAdminDtos.UpdateStatusRequest;
import com.nhom9.smartmall.user.dto.UserAdminDtos.UserSummary;
import com.nhom9.smartmall.user.service.UserAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users", description = "Quản trị tài khoản và phân quyền (chỉ BQL)")
@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserAdminService userAdminService;

    @Operation(summary = "Tìm kiếm tài khoản")
    @GetMapping
    public PageResponse<UserSummary> search(@RequestParam(required = false) Role role,
                                            @RequestParam(required = false) UserStatus status,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(defaultValue = "false") boolean unlinkedOnly,
                                            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
                                            Pageable pageable) {
        return userAdminService.search(role, status, unlinkedOnly, keyword, pageable);
    }

    @Operation(summary = "Tạo tài khoản nội bộ")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserSummary create(@Valid @RequestBody CreateUserRequest req) {
        return userAdminService.create(req);
    }

    @Operation(summary = "Gán vai trò")
    @PatchMapping("/{userId}/role")
    public UserSummary changeRole(@PathVariable UUID userId, @Valid @RequestBody UpdateRoleRequest req,
                                  @AuthenticationPrincipal Jwt jwt) {
        return userAdminService.changeRole(userId, req.role(), CurrentUser.from(jwt).id());
    }

    @Operation(summary = "Duyệt / tạm dừng tài khoản")
    @PatchMapping("/{userId}/status")
    public UserSummary changeStatus(@PathVariable UUID userId, @Valid @RequestBody UpdateStatusRequest req,
                                    @AuthenticationPrincipal Jwt jwt) {
        return userAdminService.changeStatus(userId, req.status(), CurrentUser.from(jwt).id());
    }
}
