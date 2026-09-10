package com.atamanahmet.vinylexchange.security.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtCookieUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtCookieUtil.class);

    private static final String JWT_COOKIE_NAME = "jwt";

    private static final int COOKIE_MAX_AGE_SECONDS = 60 * 60;

    /**
     * Writes the JWT cookie onto the response.
     */
    public void addJwtCookie(String token, HttpServletResponse response) {
        ResponseCookie cookie = buildCookie(token, COOKIE_MAX_AGE_SECONDS);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * Clears the JWT cookie on the response.
     */
    public void revokeJwtCookie(HttpServletResponse response) {
        ResponseCookie cookie = buildCookie("", 0);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * SameSite=None is required because frontend and backend live on different
     * subdomains of a public suffix list domain, so the browser treats them as
     * cross-site. Lax or an unset SameSite would silently drop the cookie.
     * 
     * Long-term fix is moving frontend/backend under an owned domain to 
     * make them same-site again.
     */
    private ResponseCookie buildCookie(String value, int maxAgeSeconds) {
        return ResponseCookie.from(JWT_COOKIE_NAME, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }

    /**
     * Extracts the JWT token from the request cookies.
     *
     */
    public String extractTokenFromRequest(HttpServletRequest request) {

        if (request == null || request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if (JWT_COOKIE_NAME.equals(cookie.getName())) {
                String value = cookie.getValue();
                if (!StringUtils.hasText(value)) {
                    logger.debug("JWT cookie present but value is blank");
                    return null;
                }
                return value;
            }
        }

        return null;
    }
}
