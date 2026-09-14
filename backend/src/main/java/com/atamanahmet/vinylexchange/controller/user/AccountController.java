package com.atamanahmet.vinylexchange.controller.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atamanahmet.vinylexchange.dto.user.ChangePasswordRequest;
import com.atamanahmet.vinylexchange.security.ratelimit.PasswordChangeRateLimiter;
import com.atamanahmet.vinylexchange.security.util.JwtCookieUtil;
import com.atamanahmet.vinylexchange.service.mail.AccountMailService;
import com.atamanahmet.vinylexchange.service.user.AccountService;
import com.atamanahmet.vinylexchange.session.UserUtil;
import com.atamanahmet.vinylexchange.util.ClientIpResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final AccountMailService accountMailService;
    private final PasswordChangeRateLimiter passwordChangeRateLimiter;
    private final JwtCookieUtil jwtCookieUtil;

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        var userId = UserUtil.getCurrentUserId();
        String clientIp = ClientIpResolver.resolve(httpRequest);

        passwordChangeRateLimiter.checkAllowed(userId, clientIp);

        String email = accountService.changePassword(
                userId, request.currentPassword(), request.newPassword());

        jwtCookieUtil.revokeJwtCookie(httpResponse);
        accountMailService.sendPasswordChangedEmail(email);

        return ResponseEntity.noContent().build();
    }
}
