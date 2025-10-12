package com.mik.security.filter;

import com.mik.security.token.SmsToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import java.io.IOException;
import java.util.ArrayList;

public class SmsLoginFilter extends AbstractAuthenticationProcessingFilter {

    public SmsLoginFilter() {
        super("/sms/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        String mobile = request.getParameter("mobile");
        String code = request.getParameter("code");
        SmsToken token = new SmsToken(new ArrayList<>(), mobile, code);
        return this.getAuthenticationManager().authenticate(token);
    }

}
