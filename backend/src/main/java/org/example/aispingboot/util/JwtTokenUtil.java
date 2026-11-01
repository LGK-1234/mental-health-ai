package org.example.aispingboot.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.example.aispingboot.config.JwtConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;

@Component
public class JwtTokenUtil implements ApplicationContextAware {

    private static final String ISSUER = "mental-health-assistant";

    /** 兼容旧前端使用的自定义请求头 */
    private static final String LEGACY_HEADER = "token";

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        JwtTokenUtil.applicationContext = applicationContext;
    }

    private static JwtConfig getJwtConfig() {
        return applicationContext.getBean(JwtConfig.class);
    }

    public static String generateToken(Long userId, String username, Integer roleType) {
        try {
            JwtConfig jwtConfig = getJwtConfig();
            Algorithm algorithm = Algorithm.HMAC256(jwtConfig.getSecret());
            Date expiration = new Date(System.currentTimeMillis() + jwtConfig.getExpiration());

            return JWT.create()
                    .withClaim("userId", userId)
                    .withClaim("username", username)
                    .withClaim("roleType", roleType)
                    .withExpiresAt(expiration)
                    .withIssuedAt(new Date())
                    .withIssuer(ISSUER)
                    .sign(algorithm);
        } catch (Exception e) {
            throw new IllegalStateException("生成 token 失败", e);
        }
    }

    /**
     * 提取 token：
     * 1. 优先读配置的请求头（默认 Authorization），并去除 "Bearer " 前缀；
     * 2. 兼容旧前端使用的 token 请求头。
     */
    public static String extractTokenFromRequest(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        JwtConfig jwtConfig = getJwtConfig();
        String headerName = StringUtils.hasText(jwtConfig.getHeader()) ? jwtConfig.getHeader() : "Authorization";
        String prefix = jwtConfig.getTokenPrefix();

        String headerValue = request.getHeader(headerName);
        if (!StringUtils.hasText(headerValue)) {
            headerValue = request.getHeader(LEGACY_HEADER);
            prefix = null;
        }
        if (!StringUtils.hasText(headerValue)) {
            return null;
        }

        String token = headerValue.trim();
        if (StringUtils.hasText(prefix) && token.regionMatches(true, 0, prefix, 0, prefix.length())) {
            token = token.substring(prefix.length()).trim();
        }
        return StringUtils.hasText(token) ? token : null;
    }

    public static String getCurrentToken() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        String token = (String) request.getAttribute("jwtToken");
        return StringUtils.hasText(token) ? token : extractTokenFromRequest(request);
    }

    /** 从当前请求解析用户 ID，取不到返回 null（不抛异常） */
    public static Long getCurrentUserIdOrNull() {
        try {
            String token = getCurrentToken();
            if (!StringUtils.hasText(token)) {
                return null;
            }
            return verifyToken(token).getClaim("userId").asLong();
        } catch (Exception e) {
            return null;
        }
    }

    public static TokenVerificationResult validateToken(String token) {
        DecodedJWT jwt = verifyToken(token);
        Long userId = jwt.getClaim("userId").asLong();
        String username = jwt.getClaim("username").asString();

        Integer roleType = null;
        try {
            roleType = jwt.getClaim("roleType").asInt();
        } catch (Exception e) {
            String roleTypeStr = jwt.getClaim("roleType").asString();
            if (StringUtils.hasText(roleTypeStr)) {
                roleType = Integer.valueOf(roleTypeStr);
            }
        }
        if (userId != null && StringUtils.hasText(username) && roleType != null) {
            return new TokenVerificationResult(userId, username, roleType, true);
        }
        return null;
    }

    public static DecodedJWT verifyToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new JWTVerificationException("Token不能为空");
        }
        JwtConfig jwtConfig = getJwtConfig();
        Algorithm algorithm = Algorithm.HMAC256(jwtConfig.getSecret());
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(ISSUER)
                .build();
        return verifier.verify(token);
    }

    @Getter
    public static class TokenVerificationResult {
        private final Long userId;
        private final String username;
        private final Integer roleType;
        private final boolean valid;

        public TokenVerificationResult(Long userId, String username, Integer roleType, boolean valid) {
            this.userId = userId;
            this.username = username;
            this.roleType = roleType;
            this.valid = valid;
        }
    }
}
