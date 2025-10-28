package com.proposalmanagement.config;

import com.proposalmanagement.security.CustomUserDetailsService;
import com.proposalmanagement.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                // Test endpoints - for debugging
                .requestMatchers("/test", "/swagger-test").permitAll()
                
                // Swagger UI endpoints - HIGHEST PRIORITY - Allow all patterns
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/swagger-ui/index.html").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/v3/api-docs.yaml").permitAll()
                .requestMatchers("/v3/api-docs").permitAll()
                .requestMatchers("/swagger-resources/**").permitAll()
                .requestMatchers("/webjars/**").permitAll()
                .requestMatchers("/configuration/**").permitAll()
                .requestMatchers("/swagger-config/**").permitAll()
                .requestMatchers("/api-docs/**").permitAll()
                
                // Static resources
                .requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**").permitAll()
                .requestMatchers("/*.ico", "/*.png", "/*.jpg", "/*.css", "/*.js").permitAll()
                
                // Public endpoints - no authentication required
                .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/refresh").permitAll()
                .requestMatchers("/api/auth/forgot-password", "/api/auth/reset-password", "/api/auth/verify-email").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/favicon.ico").permitAll()
                
                // ==== COMMON AUTHENTICATED ENDPOINTS (Must be before broad patterns) ====
                .requestMatchers("/api/auth/logout").authenticated()
                .requestMatchers("/api/users/profile").authenticated()
                .requestMatchers("/api/users/password").authenticated()
                .requestMatchers("/api/notifications/**").authenticated()
                .requestMatchers("/api/documents/download/**").authenticated()
                
                // ==== ADMINISTRATOR PERMISSIONS ====
                // Full system access - can do everything
                .requestMatchers("/api/auth/admin/**").hasRole("ADMIN")
                // Allow multiple roles to get all users (for reviewer assignment, team building, collaboration)
                .requestMatchers(HttpMethod.GET, "/api/users").hasAnyRole("ADMIN", "DEPARTMENT_HEAD", "COMMITTEE_CHAIR", "PROJECT_MANAGER", "PRINCIPAL_INVESTIGATOR")
                .requestMatchers(HttpMethod.GET, "/api/users/**").hasAnyRole("ADMIN", "DEPARTMENT_HEAD", "COMMITTEE_CHAIR", "PROJECT_MANAGER", "PRINCIPAL_INVESTIGATOR")
                // Allow any authenticated user to update their own profile (verification done in controller)
                .requestMatchers(HttpMethod.PUT, "/api/users/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/users").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
                .requestMatchers("/api/system/**").hasRole("ADMIN")
                .requestMatchers("/api/audit/**").hasRole("ADMIN")
                
                // ==== PROJECT MANAGER PERMISSIONS ====
                // Manage projects, assign teams, track milestones
                .requestMatchers(HttpMethod.GET, "/api/projects").hasAnyRole("ADMIN", "PROJECT_MANAGER", "PRINCIPAL_INVESTIGATOR", "DEPARTMENT_HEAD", "COMMITTEE_CHAIR")
                .requestMatchers(HttpMethod.POST, "/api/projects").hasAnyRole("ADMIN", "PROJECT_MANAGER", "PRINCIPAL_INVESTIGATOR", "COMMITTEE_CHAIR")
                .requestMatchers(HttpMethod.PUT, "/api/projects/**").hasAnyRole("ADMIN", "PROJECT_MANAGER", "PRINCIPAL_INVESTIGATOR", "COMMITTEE_CHAIR")
                .requestMatchers(HttpMethod.DELETE, "/api/projects/**").hasAnyRole("ADMIN", "PROJECT_MANAGER")
                .requestMatchers("/api/projects/*/milestones").hasAnyRole("ADMIN", "PROJECT_MANAGER", "PRINCIPAL_INVESTIGATOR")
                .requestMatchers("/api/projects/*/team").hasAnyRole("ADMIN", "PROJECT_MANAGER", "PRINCIPAL_INVESTIGATOR")
                .requestMatchers("/api/analytics/projects").hasAnyRole("ADMIN", "PROJECT_MANAGER", "DEPARTMENT_HEAD")
                
                // ==== MILESTONE PERMISSIONS ====
                // Milestone management for projects
                .requestMatchers(HttpMethod.GET, "/api/milestones/**").hasAnyRole("ADMIN", "PROJECT_MANAGER", "PRINCIPAL_INVESTIGATOR", "REVIEWER")
                .requestMatchers(HttpMethod.POST, "/api/milestones").hasAnyRole("ADMIN", "PROJECT_MANAGER", "PRINCIPAL_INVESTIGATOR")
                .requestMatchers(HttpMethod.PUT, "/api/milestones/**").hasAnyRole("ADMIN", "PROJECT_MANAGER", "PRINCIPAL_INVESTIGATOR")
                .requestMatchers(HttpMethod.DELETE, "/api/milestones/**").hasAnyRole("ADMIN", "PROJECT_MANAGER")
                
                // ==== PRINCIPAL INVESTIGATOR PERMISSIONS ====
                // Draft/submit proposals, manage documents, track milestones
                .requestMatchers(HttpMethod.GET, "/api/proposals").hasAnyRole("ADMIN", "PRINCIPAL_INVESTIGATOR", "PROJECT_MANAGER", "DEPARTMENT_HEAD", "REVIEWER", "COMMITTEE_CHAIR", "STAKEHOLDER")
                .requestMatchers(HttpMethod.POST, "/api/proposals").hasAnyRole("ADMIN", "PRINCIPAL_INVESTIGATOR")
                .requestMatchers(HttpMethod.PUT, "/api/proposals/**").hasAnyRole("ADMIN", "PRINCIPAL_INVESTIGATOR")
                .requestMatchers(HttpMethod.DELETE, "/api/proposals/**").hasAnyRole("ADMIN", "PRINCIPAL_INVESTIGATOR")
                .requestMatchers("/api/proposals/*/documents").hasAnyRole("ADMIN", "PRINCIPAL_INVESTIGATOR", "PROJECT_MANAGER")
                .requestMatchers("/api/proposals/*/team").hasAnyRole("ADMIN", "PRINCIPAL_INVESTIGATOR")
                .requestMatchers("/api/proposals/*/feedback").hasAnyRole("ADMIN", "PRINCIPAL_INVESTIGATOR", "REVIEWER")
                
                // ==== REVIEWER/EVALUATOR PERMISSIONS ====
                // Evaluate proposals, add scores and comments
                .requestMatchers(HttpMethod.GET, "/api/evaluations").hasAnyRole("ADMIN", "REVIEWER", "COMMITTEE_CHAIR", "DEPARTMENT_HEAD")
                .requestMatchers(HttpMethod.POST, "/api/evaluations").hasAnyRole("ADMIN", "REVIEWER", "COMMITTEE_CHAIR")
                .requestMatchers(HttpMethod.PUT, "/api/evaluations/**").hasAnyRole("ADMIN", "REVIEWER", "COMMITTEE_CHAIR")
                .requestMatchers("/api/evaluations/assigned").hasAnyRole("ADMIN", "REVIEWER", "COMMITTEE_CHAIR")
                .requestMatchers("/api/evaluations/*/scores").hasAnyRole("ADMIN", "REVIEWER", "COMMITTEE_CHAIR")
                .requestMatchers("/api/evaluations/*/comments").hasAnyRole("ADMIN", "REVIEWER", "COMMITTEE_CHAIR")
                
                // ==== COMMITTEE CHAIR PERMISSIONS ====
                // Assign reviewers, manage review process, oversee evaluations
                .requestMatchers("/api/evaluations/assign").hasAnyRole("ADMIN", "COMMITTEE_CHAIR")
                .requestMatchers("/api/evaluations/*/reviewers").hasAnyRole("ADMIN", "COMMITTEE_CHAIR")
                .requestMatchers("/api/evaluations/progress").hasAnyRole("ADMIN", "COMMITTEE_CHAIR", "DEPARTMENT_HEAD")
                .requestMatchers("/api/evaluations/consensus").hasAnyRole("ADMIN", "COMMITTEE_CHAIR")
                
                // ==== DEPARTMENT HEAD PERMISSIONS ====
                // Departmental oversight, approve/reject proposals
                .requestMatchers("/api/departments/*/proposals").hasAnyRole("ADMIN", "DEPARTMENT_HEAD")
                .requestMatchers("/api/departments/*/projects").hasAnyRole("ADMIN", "DEPARTMENT_HEAD", "PROJECT_MANAGER")
                .requestMatchers("/api/departments/*/users").hasAnyRole("ADMIN", "DEPARTMENT_HEAD")
                .requestMatchers("/api/departments/*/resources").hasAnyRole("ADMIN", "DEPARTMENT_HEAD")
                .requestMatchers("/api/proposals/*/approve").hasAnyRole("ADMIN", "DEPARTMENT_HEAD")
                .requestMatchers("/api/proposals/*/reject").hasAnyRole("ADMIN", "DEPARTMENT_HEAD")
                
                // ==== FINANCIAL OFFICER PERMISSIONS ====
                // Budget management, financial compliance
                .requestMatchers("/api/budget-items/**").hasAnyRole("ADMIN", "FINANCIAL_OFFICER", "PRINCIPAL_INVESTIGATOR", "PROJECT_MANAGER")
                .requestMatchers("/api/budgets/**").hasAnyRole("ADMIN", "FINANCIAL_OFFICER", "DEPARTMENT_HEAD")
                .requestMatchers("/api/budgets/approve").hasAnyRole("ADMIN", "FINANCIAL_OFFICER")
                .requestMatchers("/api/budgets/reject").hasAnyRole("ADMIN", "FINANCIAL_OFFICER")
                .requestMatchers("/api/reports/financial").hasAnyRole("ADMIN", "FINANCIAL_OFFICER", "DEPARTMENT_HEAD")
                .requestMatchers("/api/analytics/budget").hasAnyRole("ADMIN", "FINANCIAL_OFFICER")
                
                // ==== STAKEHOLDER PERMISSIONS ====
                // Limited view access to relevant proposals/projects
                .requestMatchers(HttpMethod.GET, "/api/proposals/*/summary").hasAnyRole("ADMIN", "STAKEHOLDER", "PRINCIPAL_INVESTIGATOR", "PROJECT_MANAGER", "DEPARTMENT_HEAD")
                .requestMatchers(HttpMethod.GET, "/api/projects/*/summary").hasAnyRole("ADMIN", "STAKEHOLDER", "PROJECT_MANAGER", "PRINCIPAL_INVESTIGATOR")
                .requestMatchers("/api/proposals/*/comments").hasAnyRole("ADMIN", "STAKEHOLDER", "REVIEWER", "PRINCIPAL_INVESTIGATOR")
                

                
                // ==== GENERAL DEPARTMENT ACCESS ====
                // Allow all authenticated users to read departments (for dropdowns in forms)
                .requestMatchers(HttpMethod.GET, "/api/departments").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/departments/**").authenticated()
                // Only specific roles can create, update, or delete departments
                .requestMatchers(HttpMethod.POST, "/api/departments").hasAnyRole("ADMIN", "DEPARTMENT_HEAD")
                .requestMatchers(HttpMethod.PUT, "/api/departments/**").hasAnyRole("ADMIN", "DEPARTMENT_HEAD")
                .requestMatchers(HttpMethod.DELETE, "/api/departments/**").hasAnyRole("ADMIN")
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)); // For H2 console if needed
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allow specific origins including localhost frontend and deployed Vercel frontend
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000", 
            "http://127.0.0.1:3000",
            "http://localhost:8080",
            "http://127.0.0.1:8080",
            "https://project-proposal-management-system.vercel.app"
        ));
        // Allow all methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"));
        // Allow all headers
        configuration.setAllowedHeaders(Arrays.asList("*"));
        // Allow credentials
        configuration.setAllowCredentials(true);
        // Expose headers that frontend might need
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Total-Count"));
        // Set max age for preflight requests
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}