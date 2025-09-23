package huy.example.demoMonday.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class IpUtil {
    private IpUtil(){}

    public static String clientIp(){
        var attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attr == null) return "unknown";
        HttpServletRequest req = attr.getRequest();

        String ip = header(req, "X-Forwarded-For");
        if (ip == null) ip = header(req, "X-Real-IP");
        if (ip == null) ip = req.getRemoteAddr();
        if (ip == null) ip = "unknown";

        // Nếu có list "client, proxy1, proxy2", lấy client đầu tiên
        int comma = ip.indexOf(',');
        if (comma > 0) ip = ip.substring(0, comma).trim();
        return ip;
    }

    private static String header(HttpServletRequest req, String name){
        String v = req.getHeader(name);
        return (v == null || v.isBlank()) ? null : v.trim();
    }
}
