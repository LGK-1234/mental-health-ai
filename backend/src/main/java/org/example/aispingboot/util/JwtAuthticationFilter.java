package org.example.aispingboot.util;

import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.aispingboot.DTO.response.UserLoginResponseDTO;
import org.example.aispingboot.common.ResultCode;
import org.example.aispingboot.config.SecurityConfig;
import org.example.aispingboot.enumClass.UserStatus;
import org.example.aispingboot.service.UserService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class JwtAuthticationFilter extends OncePerRequestFilter {

    @Resource
    private UserService userService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return SecurityConfig.isPublicPATH(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String token = JwtTokenUtil.extractTokenFromRequest(request);

        // 未携带 token：直接 401 结束，不再放行
        if (!StringUtils.hasText(token)) {
            clearSecurityContext();
            ResponseUtil.writeError(response, ResultCode.ACCESS_UNAUTHORIZED);
            return;
        }

        try {
            JwtTokenUtil.TokenVerificationResult result = JwtTokenUtil.validateToken(token);
            if (result == null || !result.isValid()) {
                clearSecurityContext();
                ResponseUtil.writeError(response, ResultCode.TOKEN_INVALID);
                return;
            }

            UserLoginResponseDTO.UserDetailResponseDTO user = userService.getUserById(result.getUserId());
            if (user == null || !UserStatus.NORMAL.getCode().equals(user.getStatus())) {
                clearSecurityContext();
                ResponseUtil.writeError(response, ResultCode.TOKEN_ACCESS_FORBIDDEN);
                return;
            }

            List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + result.getRoleType()));
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    result.getUsername(), null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.setAttribute("jwtToken", token);

            chain.doFilter(request, response);
        } catch (Exception e) {
            // token 过期 / 签名错误 / 用户被删等统一按未授权处理，避免 500
            clearSecurityContext();
            ResponseUtil.writeError(response, ResultCode.TOKEN_INVALID);
        }
    }

    private void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}
