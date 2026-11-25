package com.chuange.diy.backend.spring_config.security;

import com.chuange.diy.backend.model.db.ShopUser;
import com.chuange.diy.backend.repository.ShopUserRepository;
import com.chuange.diy.backend.spring_config.TestDataGenerator;
import com.chuange.diy.backend.tool.JwtTool;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTool jwtTool;
    private final ShopUserRepository shopUserRepository;

    public JwtAuthenticationFilter(JwtTool jwtTool, ShopUserRepository shopUserRepository) {
        this.jwtTool = jwtTool;
        this.shopUserRepository = shopUserRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            if (TestDataGenerator.DEV_OMNI_TOKEN.equals(token)) {
                ShopUser admin = resolveOmniAdmin();
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ADMIN"));
                authorities.add(new SimpleGrantedAuthority("USER"));
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(admin, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
                filterChain.doFilter(request, response);
                return;
            }

            try {
                Claims claims = jwtTool.parseToken(token);
                Long userId = Long.valueOf(claims.getSubject());
                ShopUser user = shopUserRepository.findById(userId).orElse(null);
                if (user != null) {
                    String role = user.role != null ? user.role.trim().toUpperCase() : "USER";
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority(role));
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(user, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (JwtException | NumberFormatException ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    private ShopUser resolveOmniAdmin() {
        ShopUser found = shopUserRepository.findByUsername(TestDataGenerator.DEV_ADMIN_USERNAME)
                .orElseGet(() -> shopUserRepository.findAll().stream()
                        .filter(u -> u.role != null && "ADMIN".equalsIgnoreCase(u.role.trim()))
                        .findFirst()
                        .orElse(null));
        if (found != null) {
            return found;
        }
        ShopUser fallback = new ShopUser();
        fallback.id = 0L;
        fallback.username = TestDataGenerator.DEV_ADMIN_USERNAME;
        fallback.name = "开发全能管理员";
        fallback.role = "ADMIN";
        return fallback;
    }
}