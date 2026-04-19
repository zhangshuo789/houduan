package com.volleyball.volleyballcommunitybackend.config;

import com.volleyball.volleyballcommunitybackend.entity.SysRole;
import com.volleyball.volleyballcommunitybackend.entity.SysUserRole;
import com.volleyball.volleyballcommunitybackend.repository.SysRoleRepository;
import com.volleyball.volleyballcommunitybackend.repository.SysUserRoleRepository;
import com.volleyball.volleyballcommunitybackend.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(JwtAuthenticationFilter jwtAuthenticationFilter, HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
           .cors(cors -> {})  // 启用 CORS
           .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
           .exceptionHandling(ex -> ex
                   .authenticationEntryPoint(new HttpStatusEntryPoint())
           )
           .authorizeHttpRequests(auth -> auth
                   .requestMatchers("/api/auth/**").permitAll()
                   .requestMatchers("/api/boards/**").permitAll()
                   .requestMatchers("/api/file/**").permitAll()
                   .requestMatchers(HttpMethod.GET, "/api/post/**").permitAll()
                   .requestMatchers(HttpMethod.GET, "/api/user/**").permitAll()
                   .requestMatchers("/api/follow/**").authenticated()
                   .requestMatchers("/api/message/**").authenticated()
                   .requestMatchers("/api/group/**").authenticated()
                   .requestMatchers("/api/sse/connect").permitAll()
                   .requestMatchers(HttpMethod.GET, "/api/event").permitAll()
                   .requestMatchers(HttpMethod.GET, "/api/event/{id}").permitAll()
                   .requestMatchers(HttpMethod.POST, "/api/event").authenticated()
                   .requestMatchers(HttpMethod.PUT, "/api/event/{id}").authenticated()
                   .requestMatchers(HttpMethod.DELETE, "/api/event/{id}").hasRole("ADMIN")
                   .requestMatchers(HttpMethod.POST, "/api/admin/reports").authenticated()  // 提交举报-需登录
                   .requestMatchers(HttpMethod.GET, "/api/admin/reports").hasRole("ADMIN")  // 举报列表-需管理员
                   .requestMatchers(HttpMethod.POST, "/api/admin/notification/send").hasRole("ADMIN")  // 发送通知-需管理员
                   .requestMatchers("/api/admin/notification/**").authenticated()  // 通知列表/已读-需登录
                   .requestMatchers("/api/admin/**").hasRole("ADMIN")  // 其他管理员接口
                   .requestMatchers(HttpMethod.POST, "/api/event/{id}/subscribe").authenticated()
                   .requestMatchers(HttpMethod.DELETE, "/api/event/{id}/subscribe").authenticated()
                   .requestMatchers(HttpMethod.POST, "/api/event/{id}/register").authenticated()
                   .requestMatchers(HttpMethod.GET, "/api/event/{id}/registration").authenticated()
                   .requestMatchers(HttpMethod.PUT, "/api/event/{id}/registration/{regId}").authenticated()
                   .requestMatchers(HttpMethod.POST, "/api/event/*/images").authenticated()
                   .requestMatchers(HttpMethod.GET, "/api/event/*/images").permitAll()
                   .requestMatchers(HttpMethod.GET, "/api/user/*/following").permitAll()
                   .requestMatchers(HttpMethod.GET, "/api/user/*/followers").permitAll()
                   .requestMatchers(HttpMethod.GET, "/api/user/*/friends").permitAll()
                   .requestMatchers(HttpMethod.GET, "/api/user/*/stats").permitAll()
                   .anyRequest().authenticated()
           )
           .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * 未登录时返回401
     */
    public static class HttpStatusEntryPoint implements AuthenticationEntryPoint {
        @Override
        public void commence(HttpServletRequest request, HttpServletResponse response,
                           org.springframework.security.core.AuthenticationException authException) throws IOException {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.print("{\"code\":401,\"message\":\"请先登录\",\"data\":null}");
            out.flush();
        }
    }

    @Component
    public static class JwtAuthenticationFilter extends OncePerRequestFilter {

        private final JwtUtil jwtUtil;
        private final SysUserRoleRepository sysUserRoleRepository;
        private final SysRoleRepository sysRoleRepository;

        public JwtAuthenticationFilter(JwtUtil jwtUtil, SysUserRoleRepository sysUserRoleRepository,
                                       SysRoleRepository sysRoleRepository) {
            this.jwtUtil = jwtUtil;
            this.sysUserRoleRepository = sysUserRoleRepository;
            this.sysRoleRepository = sysRoleRepository;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtUtil.validateToken(token)) {
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    String username = jwtUtil.getUsernameFromToken(token);

                    // 加载用户角色
                    List<SysUserRole> userRoles = sysUserRoleRepository.findByUserId(userId);
                    List<GrantedAuthority> authorities = userRoles.stream()
                            .map(ur -> {
                                String roleName = sysRoleRepository.findById(ur.getRoleId())
                                        .map(SysRole::getName)
                                        .orElse("USER");
                                return new SimpleGrantedAuthority("ROLE_" + roleName);
                            })
                            .collect(Collectors.toList());

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, username, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

            filterChain.doFilter(request, response);
        }
    }
}
