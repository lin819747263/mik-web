package com.mik.gateway.filter;


import com.mik.gateway.token.SmsCodeToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import java.io.IOException;
import java.util.ArrayList;

//@Configuration
public class SmsCodeFilter extends AbstractAuthenticationProcessingFilter {

    public SmsCodeFilter(String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        String username = request.getAttribute("username").toString();
        String password = request.getAttribute("password").toString();
        Authentication token = new SmsCodeToken(new ArrayList<>(), username, password);

        return token;
    }
}
