package com.kreuterkeule.PEM.services;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class HttpRequestService {

    public static String getIpFromRequest(HttpServletRequest request) {
        String remoteAddr;
        remoteAddr = request.getHeader("X-FORWARDED-FOR");
        if (remoteAddr == null || "".equals(remoteAddr)) {
            remoteAddr = request.getRemoteAddr();
        }
        return remoteAddr;
    }

}
