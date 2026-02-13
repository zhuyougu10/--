package com.stadium.booking.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserContext {
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
            return ((UserPrincipal) auth.getPrincipal()).getUserId();
        }
        return null;
    }

    public static String getCurrentUserType() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
            return ((UserPrincipal) auth.getPrincipal()).getUserType();
        }
        return null;
    }

    public static boolean isCurrentUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
            return ((UserPrincipal) auth.getPrincipal()).getIsAdmin();
        }
        return false;
    }

    public static UserPrincipal getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
            return (UserPrincipal) auth.getPrincipal();
        }
        return null;
    }
}
