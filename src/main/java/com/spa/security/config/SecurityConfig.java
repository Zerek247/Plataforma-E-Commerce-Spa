@Bean
public SecurityFilterChain securityFilterChain(
        org.springframework.security.config.annotation.web.builders.HttpSecurity http) throws Exception {

    http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.disable())   // ← DESACTIVAMOS CORS EN SECURITY
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                    // Preflight CORS
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                    // Autenticación pública
                    .requestMatchers("/api/auth/**").permitAll()

                    // Catálogo público
                    .requestMatchers(HttpMethod.GET,
                            "/api/products", "/api/products/**",
                            "/api/categories", "/api/categories/**",
                            "/api/spa-services", "/api/spa-services/**",
                            "/api/service-categories", "/api/service-categories/**",
                            "/api/services", "/api/services/**"
                    ).permitAll()

                    // Contacto/email
                    .requestMatchers(HttpMethod.POST, "/api/email/enviar").permitAll()

                    // Reservas
                    .requestMatchers(HttpMethod.POST, "/api/reservas/**")
                    .hasAnyAuthority("ROLE_USER","ROLE_ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/reservas/**")
                    .hasAnyAuthority("ROLE_USER","ROLE_ADMIN")

                    // Órdenes
                    .requestMatchers("/api/orders/**")
                    .hasAnyAuthority("ROLE_USER","ROLE_ADMIN")

                    // Usuarios
                    .requestMatchers(HttpMethod.GET, "/api/usuarios/**")
                    .hasAnyAuthority("ROLE_USER","ROLE_ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/usuarios/me/password")
                    .hasAnyAuthority("ROLE_USER","ROLE_ADMIN")

                    // Admin
                    .requestMatchers(HttpMethod.POST,
                            "/api/products/**","/api/categories/**",
                            "/api/services/**","/api/service-categories/**"
                    ).hasAuthority("ROLE_ADMIN")

                    .requestMatchers(HttpMethod.PUT,
                            "/api/products/**","/api/categories/**",
                            "/api/services/**","/api/service-categories/**"
                    ).hasAuthority("ROLE_ADMIN")

                    .requestMatchers(HttpMethod.DELETE,
                            "/api/products/**","/api/categories/**",
                            "/api/services/**","/api/service-categories/**"
                    ).hasAuthority("ROLE_ADMIN")

                    // Cualquier otra cosa requiere autenticación
                    .anyRequest().authenticated()
            )
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
