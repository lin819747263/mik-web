package com.mik.auth.filter;

import com.mik.auth.token.UsernamePasswordToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import java.io.IOException;
import java.util.ArrayList;

public class UsernamePasswordFilter extends AbstractAuthenticationProcessingFilter {

    public UsernamePasswordFilter() {
        super("/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        String mobile = request.getParameter("username");
        String code = request.getParameter("password");
        UsernamePasswordToken token = new UsernamePasswordToken(new ArrayList<>(), mobile, code);
        return this.getAuthenticationManager().authenticate(token);
    }
}
