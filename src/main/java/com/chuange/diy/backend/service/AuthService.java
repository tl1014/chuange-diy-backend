package com.chuange.diy.backend.service;

import com.chuange.diy.backend.model.db.ShopUser;
import com.chuange.diy.backend.repository.ShopUserRepository;
import com.chuange.diy.backend.tool.JwtTool;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class AuthService {

    private final ShopUserRepository shopUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTool jwtTool;

    public AuthService(ShopUserRepository shopUserRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTool jwtTool) {
        this.shopUserRepository = shopUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTool = jwtTool;
    }

    @Transactional
    public Map<String, Object> register(String username, String email, String password,
                                        String name, String phone, String address) {
        if (username == null || username.isBlank() || password == null || password.length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "用户名不能为空且密码至少 6 位");
        }
        String cleanUsername = username.trim();
        if (shopUserRepository.existsByUsername(cleanUsername)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "用户名已存在");
        }
        String cleanEmail = (email == null || email.isBlank()) ? null : email.trim();
        if (cleanEmail != null && shopUserRepository.existsByEmail(cleanEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "邮箱已被注册");
        }

        ShopUser user = new ShopUser();
        user.username = cleanUsername;
        user.email = cleanEmail;
        user.passwordHash = passwordEncoder.encode(password);
        user.role = "USER";
        user.name = (name == null || name.isBlank()) ? cleanUsername : name.trim();
        user.phone = (phone == null) ? "" : phone.trim();
        user.address = (address == null) ? "" : address.trim();

        ShopUser saved = shopUserRepository.save(user);
        return buildAuthPayload(saved);
    }

    public Map<String, Object> login(String account, String password) {
        if (account == null || account.isBlank() || password == null || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "账号和密码不能为空");
        }
        String cleanAccount = account.trim();
        ShopUser user = shopUserRepository.findByUsername(cleanAccount)
                .or(() -> shopUserRepository.findByEmail(cleanAccount))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "账号或密码错误"));

        if (user.passwordHash == null || !passwordEncoder.matches(password, user.passwordHash)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "账号或密码错误");
        }

        return buildAuthPayload(user);
    }

    public Map<String, Object> getProfile(ShopUser principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        ShopUser user = shopUserRepository.findById(principal.id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户不存在"));
        return Map.of(
                "userId", user.id,
                "username", user.username == null ? "" : user.username,
                "email", user.email == null ? "" : user.email,
                "name", user.name == null ? "" : user.name,
                "phone", user.phone == null ? "" : user.phone,
                "address", user.address == null ? "" : user.address,
                "role", user.role
        );
    }

    private Map<String, Object> buildAuthPayload(ShopUser user) {
        String token = jwtTool.generateToken(user);
        return Map.of(
                "token", token,
                "userId", user.id,
                "username", user.username == null ? "" : user.username,
                "name", user.name == null ? "" : user.name,
                "role", user.role
        );
    }
}