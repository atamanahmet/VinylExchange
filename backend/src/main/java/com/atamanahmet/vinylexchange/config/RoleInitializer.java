package com.atamanahmet.vinylexchange.config;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.atamanahmet.vinylexchange.service.user.AuthService;
import com.atamanahmet.vinylexchange.domain.entity.Role;
import com.atamanahmet.vinylexchange.service.user.RoleService;
import com.atamanahmet.vinylexchange.domain.enums.RoleName;

import com.atamanahmet.vinylexchange.service.user.UserService;

import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

/**
 * Runs on every startup before demo data init.
 * Seeds roles into DB if not present. Required for user registration to work.
 */
@Component
@Slf4j
@Order(1)
@RequiredArgsConstructor
public class RoleInitializer implements ApplicationRunner {

        private final RoleService roleService;
        private final AuthService authService;
        private final UserService userService;

        private static final Map<RoleName, String> ROLE_DESCRIPTIONS = Map.of(
                        RoleName.ROLE_USER, "Regular user with basic permissions",
                        RoleName.ROLE_ADMIN, "Administrator with full system access",
                        RoleName.ROLE_MODERATOR, "Moderator with content management permissions");

        @Override
        @Transactional
        public void run(ApplicationArguments args) {
                log.info("Initializing roles...");

                ROLE_DESCRIPTIONS.forEach((roleName, description) -> {
                        if (!roleService.existsByName(roleName)) {

                                Role role = Role.builder()
                                                .name(roleName)
                                                .description(description)
                                                .build();
                                roleService.saveRole(role);

                            log.info("Role created: {}", roleName);
                        } else {
                            log.info("Role already exists: {}", roleName);
                        }
                });
                log.info("Role initialization completed.");
        }
}
