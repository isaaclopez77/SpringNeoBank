package com.springneobank.auth.common;

import jakarta.servlet.http.HttpServletRequest;

public class Utils {

    /**
     * Get AuthorizationHeader
     * Nullable
     * 
     * @param request
     * @return
     */
    public static String getAuthorizationHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        return (authHeader != null && !authHeader.isBlank()) ? authHeader : null;
    }

}
