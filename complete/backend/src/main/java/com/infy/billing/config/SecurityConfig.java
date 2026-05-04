package com.infy.billing.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.infy.billing.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

   @Bean
   public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
       http
           .cors(cors -> cors.configurationSource(corsConfigurationSource()))
           .csrf(csrf -> csrf
               .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
               .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
           )
           .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
           .securityContext(context -> context.requireExplicitSave(false))
           .authorizeHttpRequests(auth -> auth
        		   .requestMatchers("/error").permitAll()
        		   .requestMatchers("/api/customer/login", "/api/customer/register").permitAll()
        		   .requestMatchers("/api/customer/plans/featured", "/api/customer/plans/all").permitAll()
        		   .requestMatchers("/api/manager/login").permitAll()
        		   .requestMatchers("/logout").permitAll()
        		   .requestMatchers("/api/auth/me", "/api/auth/expired").permitAll()
        		   .requestMatchers("/").permitAll()
        		   .requestMatchers("/api/**").authenticated()
               // All other endpoints require the JSESSIONID cookie presence
        		   .anyRequest().authenticated()
        		   )
           .exceptionHandling(ex -> ex
        		   .authenticationEntryPoint((request, response, authException) -> {
        			   response.setStatus(401);
        			   response.setContentType("application/json");
        			   response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
        		   })
        		   )
           .logout(logout -> logout
        		   .logoutUrl("/logout")
        		   .logoutSuccessHandler((request, response, authentication) -> {
        			   response.setStatus(200);
        			   response.setContentType("application/json");
        			   response.getWriter().write("{\"success\":true,\"message\":\"Logout successful\",\"redirect\":\"/\"}");
        		   })
        		   .invalidateHttpSession(true)
        		   .deleteCookies("JSESSIONID")
        		   .permitAll()
        		   )
           .sessionManagement(session -> session
        		   .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
        		   .sessionFixation().migrateSession()
        		   .maximumSessions(1).expiredUrl("/api/auth/expired"));
       
       return http.build();
   }

   @Bean
   public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
       return config.getAuthenticationManager();
   }

   @Bean
   public PasswordEncoder passwordEncoder() {
       return new BCryptPasswordEncoder(); // Built-in Spring hashing
   }

   @Bean
   CorsConfigurationSource corsConfigurationSource() {
       CorsConfiguration configuration = new CorsConfiguration();
       // Allow both direct access and proxied access
       configuration.setAllowedOriginPatterns(List.of(
           "http://localhost:3000",
           "http://127.0.0.1:3000"
       ));
       configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
       configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin",
    		   "X-Requested-With", "X-XSRF-TOKEN",
    		   "Access-Control-Request-Method",
    		   "Access-Control-Request-Headers"));
       configuration.setExposedHeaders(Arrays.asList("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials", "Set-Cookie"));
       configuration.setAllowCredentials(true); // REQUIRED FOR SESSION COOKIES

       UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
       source.registerCorsConfiguration("/**", configuration);
       return source;
   }
   
   @Bean
   public UserDetailsService userDetailsService(UserRepository userRepository) {
	   return username -> userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
   }
   
   @Bean
   public AuthenticationProvider authenticationProvider(UserRepository userRepository) {
	   DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
	   provider.setUserDetailsService(userDetailsService(userRepository));
	   provider.setPasswordEncoder(passwordEncoder());
	   return provider;
   }

   /**
    * SPA-compatible CSRF token request handler.
    * 
    * SPAs need special handling because the browser cannot submit the token via a form field.
    * Instead, the token is read from a cookie and sent as a header (X-XSRF-TOKEN).
    * 
    * This handler uses XorCsrfTokenRequestAttributeHandler for BREACH protection on the
    * token value, while using CsrfTokenRequestAttributeHandler for resolving the actual
    * token from the request header (since the SPA sends the raw cookie value, not XOR'd).
    */
   static final class SpaCsrfTokenRequestHandler extends CsrfTokenRequestAttributeHandler {
       private final CsrfTokenRequestHandler delegate = new XorCsrfTokenRequestAttributeHandler();

       @Override
       public void handle(HttpServletRequest request, HttpServletResponse response,
               Supplier<CsrfToken> csrfToken) {
           // Always use XorCsrfTokenRequestAttributeHandler to provide BREACH protection
           this.delegate.handle(request, response, csrfToken);
       }

       @Override
       public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
           // If the token is in the header (SPA pattern), resolve it as a raw value
           if (StringUtils.hasText(request.getHeader(csrfToken.getHeaderName()))) {
               return super.resolveCsrfTokenValue(request, csrfToken);
           }
           // Otherwise, use the XOR handler (for traditional form submissions)
           return this.delegate.resolveCsrfTokenValue(request, csrfToken);
       }
   }

   /**
    * Filter that ensures the CSRF token cookie is always present in the response.
    * 
    * Spring Security uses deferred CSRF tokens — the token is only generated when
    * it is first accessed. This filter forces the token to be loaded on every request
    * so the cookie is consistently available for the SPA to read.
    */
   static final class CsrfCookieFilter extends OncePerRequestFilter {
       @Override
       protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
               FilterChain filterChain) throws ServletException, IOException {
           CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
           // Force the token to be loaded so Spring Security writes the cookie
           if (csrfToken != null) {
               csrfToken.getToken();
           }
           filterChain.doFilter(request, response);
       }
   }
   
}