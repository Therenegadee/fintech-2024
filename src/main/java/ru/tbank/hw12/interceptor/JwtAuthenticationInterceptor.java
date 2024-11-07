package ru.tbank.hw12.interceptor;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.tbank.hw12.entity.Token;
import ru.tbank.hw12.exception.BadRequestException;
import ru.tbank.hw12.exception.ExceptionsHandler;
import ru.tbank.hw12.exception.NotAuthorizedAccessException;
import ru.tbank.hw12.repository.TokenRepository;
import ru.tbank.hw12.v1.service.JwtService;
import ru.tbank.hw12.v1.service.UserService;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationInterceptor extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserService userDetailsService;
    private final TokenRepository tokenRepository;
    private final ExceptionsHandler exceptionsHandler;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        Optional<Token> token = tokenRepository.findById(jwt);

        if (token.isPresent() && token.get().isBlackListed()) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write("Данный токен больше не действителен!");
            return;
        }

        try {
            final String username = jwtService.extractUsername(jwt);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (username != null && authentication == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            String errorMessage = "Произошла ошибка при извлечении информации из токена.";
            log.error("{}. Причина: {}.\nStackTrace: {}", errorMessage, ExceptionUtils.getMessage(e), ExceptionUtils.getStackTrace(e));
            throw new NotAuthorizedAccessException(errorMessage, e);
        }
    }
}
