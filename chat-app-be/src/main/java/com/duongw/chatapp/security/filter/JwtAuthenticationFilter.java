package com.duongw.chatapp.security.filter;

import com.duongw.chatapp.security.auth.CustomsUserDetailService;
import com.duongw.chatapp.security.token.JwtTokenProvider;
import com.duongw.chatapp.utils.StringUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
@RequiredArgsConstructor

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomsUserDetailService customsUserDetailService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwtToken = StringUtil.getJwtFromRequest(request);

            if (StringUtils.hasText(jwtToken) && jwtTokenProvider.validateJwtToken(jwtToken)) {
                String email = jwtTokenProvider.getEmailFromJwtToken(jwtToken);

                UserDetails userDetails = customsUserDetailService.loadUserByUsername(email);
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                authenticationToken.setDetails(new org.springframework.security.web.authentication.WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().getAuthentication();
            }
        } catch (Exception ex) {
            log.error("Failed to set user authentication in security context", ex);
        }
        filterChain.doFilter(request, response);
    }
}
