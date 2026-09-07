package com.rooonnie.ecomm.auth;

import com.rooonnie.ecomm.common.ForbiddenException;
import com.rooonnie.ecomm.common.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthGuard {

    public Long requireUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal principal)) {
            throw new UnauthorizedException("Authentication required");
        }
        return principal.userId();
    }

    public void requireUser(Long userId) {
        if (!requireUserId().equals(userId)) {
            throw new ForbiddenException("You can only access your own account");
        }
    }
}
