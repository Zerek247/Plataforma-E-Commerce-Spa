package com.spa.security.config;

import com.spa.security.jwt.JwtUtil;
import com.spa.security.service.UsuarioDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UsuarioDetailsServiceImpl userDetailsService;

    public JwtRequestFilter(JwtUtil jwtUtil, UsuarioDetailsServiceImpl userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // ===========================================================
        //  RUTAS PÚBLICAS (solo GET para productos y categorías)
        // ===========================================================
        if (
            // GET públicos
                (method.equals("GET") && path.startsWith("/api/products"))
                        || (method.equals("GET") && path.startsWith("/api/categories"))
                        || (method.equals("GET") && path.startsWith("/api/spa-services"))
                        || (method.equals("GET") && path.startsWith("/api/service-categories"))

                        // Login y register
                        || path.startsWith("/api/auth/")

                        // Envío de correos
                        || path.startsWith("/api/email/")
        ) {
            filterChain.doFilter(request, response);
            return;
        }

        // ===========================================================
        //  PERMITIR OPTIONS SIN TOKEN (para CORS)
        // ===========================================================
        if ("OPTIONS".equalsIgnoreCase(method)) {
            response.setStatus(HttpServletResponse.SC_OK);
            filterChain.doFilter(request, response);
            return;
        }

        // ===========================================================
        //  PROCESAR TOKEN JWT
        // ===========================================================

        final String authHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);

            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                System.out.println("Error leyendo JWT: " + e.getMessage());
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            var userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtUtil.isTokenValid(jwt, userDetails.getUsername())) {

                // Extraer el rol del token
                String role = jwtUtil.extractRole(jwt);
                String normalizedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;

                var authorities = List.of(new SimpleGrantedAuthority(normalizedRole));

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
