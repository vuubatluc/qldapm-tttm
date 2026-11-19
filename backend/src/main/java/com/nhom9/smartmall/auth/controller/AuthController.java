package com.nhom9.smartmall.auth.controller;

import com.nhom9.smartmall.auth.dto.AuthDtos.LoginRequest;
import com.nhom9.smartmall.auth.dto.AuthDtos.RefreshRequest;
import com.nhom9.smartmall.auth.dto.AuthDtos.RegisterRequest;
import com.nhom9.smartmall.auth.dto.AuthDtos.TokenResponse;
import com.nhom9.smartmall.auth.dto.AuthDtos.UserInfo;
import com.nhom9.smartmall.auth.service.AuthService;
import com.nhom9.smartmall.auth.service.AuthService.ClientInfo;
import com.nhom9.smartmall.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "Đăng nhập, đăng ký, làm mới phiên")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Đăng nhập bằng email + mật khẩu")
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest req, HttpServletRequest http) {
        return authService.login(req, new ClientInfo(http.getRemoteAddr(), http.getHeader(HttpHeaders.USER_AGENT)));
    }

    @Operation(summary = "Đăng ký tài khoản khách thuê (chờ BQL duyệt)")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserInfo register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @Operation(summary = "Cấp lại access token từ refresh token")
    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest req) {
        return authService.refresh(req.refreshToken());
    }

    @Operation(summary = "Thông tin tài khoản đang đăng nhập")
    @GetMapping("/me")
    public UserInfo me(@AuthenticationPrincipal Jwt jwt) {
        return authService.me(CurrentUser.from(jwt).id());
    }
}
