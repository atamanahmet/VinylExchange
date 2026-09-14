package com.atamanahmet.vinylexchange.service.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Outbound email. Disabled unless MAIL_ENABLED=true.
 * Failures are logged only; callers must not treat send as required for success.
 */
@Service
public class AccountMailService {

    private static final Logger logger = LoggerFactory.getLogger(AccountMailService.class);

    private static final String PASSWORD_CHANGED_SUBJECT = "Your password was changed";
    private static final String PASSWORD_CHANGED_BODY =
            "Your password was changed. If this wasn't you, contact support immediately.";

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final boolean mailEnabled;
    private final String fromAddress;

    public AccountMailService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.mail.enabled:false}") boolean mailEnabled,
            @Value("${app.mail.from:}") String fromAddress) {
        this.mailSenderProvider = mailSenderProvider;
        this.mailEnabled = mailEnabled;
        this.fromAddress = fromAddress;
    }

    public void sendPasswordChangedEmail(String toAddress) {
        if (!mailEnabled) {
            logger.debug("Password-changed email skipped: mail disabled (MAIL_ENABLED=false)");
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null || !StringUtils.hasText(fromAddress) || !StringUtils.hasText(toAddress)) {
            logger.warn("Password-changed email skipped: mail sender or from/to address not configured");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toAddress);
            message.setSubject(PASSWORD_CHANGED_SUBJECT);
            message.setText(PASSWORD_CHANGED_BODY);
            mailSender.send(message);
        } catch (Exception exception) {
            logger.error("Failed to send password-changed email", exception);
        }
    }
}
