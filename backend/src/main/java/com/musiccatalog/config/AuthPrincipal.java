package com.musiccatalog.config;

import java.security.Principal;

public record AuthPrincipal(Long userId, String email) implements Principal {
    @Override
    public String getName() {
        return email;
    }
}
