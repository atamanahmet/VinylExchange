package com.atamanahmet.vinylexchange.security.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.auth0.jwt.exceptions.JWTVerificationException;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;

@Component
public class JwtCookieUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtCookieUtil.class);

    private static final String JWT_COOKIE_NAME = "jwt";

    private static final int COOKIE_MAX_AGE_SECONDS = 60 * 60;

    public Cookie createJwtCookie(String token) {

        Cookie cookie = new Cookie("jwt", token);

        cookie.setHttpOnly(true);
        cookie.setSecure(true); // https
        cookie.setPath("/");
        cookie.setMaxAge(COOKIE_MAX_AGE_SECONDS);
//        cookie.setAttribute("SameSite", "Strict");

        return cookie;
    }

    public void revokeJwtCookie(HttpServletResponse response) {

        Cookie cookie = new Cookie("jwt", null);

        cookie.setHttpOnly(true);
        cookie.setSecure(true); // https
        cookie.setPath("/");
        cookie.setMaxAge(0); // cookie removal
//        cookie.setAttribute("SameSite", "Strict");


        response.addCookie(cookie);
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
