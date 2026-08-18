package com.grits.paymentservice.security;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

@UtilityClass
public class SecurityHelper {

    public static boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    public static boolean isNotAuthenticated(Authentication authentication) {
        return authentication == null || authentication.getPrincipal() == null;
    }
}
