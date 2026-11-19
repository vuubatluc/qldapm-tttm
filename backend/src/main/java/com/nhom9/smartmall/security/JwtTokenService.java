package com.nhom9.smartmall.security;

import com.nhom9.smartmall.auth.entity.UserAccount;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

/** Sinh và đọc JWT. Access token 2 giờ, refresh token 7 ngày (UC-TK01 bước 9). */
@Service
public class JwtTokenService {

    public static final String CLAIM_TYPE = "typ";
    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_NAME = "name";
    public static final String CLAIM_TENANT = "tenantId";
    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder refreshTokenDecoder;
    private final JwtProperties props;

    public JwtTokenService(JwtEncoder jwtEncoder, SecretKey jwtSecretKey, JwtProperties props) {
        this.jwtEncoder = jwtEncoder;
        this.props = props;
        // Decoder riêng cho refresh token; decoder của resource server chỉ nhận access token.
        this.refreshTokenDecoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256).build();
    }

    public IssuedToken issueAccessToken(UserAccount user) {
        Instant now = Instant.now();
        Instant exp = now.plus(props.accessTokenTtl());
        JwtClaimsSet.Builder claims = baseClaims(user, now, exp, TYPE_ACCESS)
                .claim(CLAIM_NAME, user.getFullName())
                .claim(CLAIM_ROLES, List.of(user.getRole().name()));
        if (user.getTenantId() != null) {
            claims.claim(CLAIM_TENANT, user.getTenantId().toString());
        }
        return new IssuedToken(encode(claims.build()), exp);
    }

    public IssuedToken issueRefreshToken(UserAccount user) {
        Instant now = Instant.now();
        Instant exp = now.plus(props.refreshTokenTtl());
        return new IssuedToken(encode(baseClaims(user, now, exp, TYPE_REFRESH).build()), exp);
    }

    /** Trả về userId nếu refresh token hợp lệ, ngược lại ném {@link JwtException}. */
    public UUID parseRefreshToken(String token) {
        Jwt jwt = refreshTokenDecoder.decode(token);
        if (!TYPE_REFRESH.equals(jwt.getClaimAsString(CLAIM_TYPE))) {
            throw new JwtException("Not a refresh token");
        }
        return UUID.fromString(jwt.getSubject());
    }

    private JwtClaimsSet.Builder baseClaims(UserAccount user, Instant now, Instant exp, String type) {
        return JwtClaimsSet.builder()
                .issuer(props.issuer())
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(exp)
                .id(UUID.randomUUID().toString())
                .claim(CLAIM_TYPE, type);
    }

    private String encode(JwtClaimsSet claims) {
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public record IssuedToken(String value, Instant expiresAt) {
    }
}
