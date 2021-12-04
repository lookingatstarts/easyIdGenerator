package com.easy.id.web.config;

import com.alibaba.fastjson.JSON;
import com.easy.id.web.resp.ApiResponse;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

@Component
@WebFilter(filterName = "escapeExceptionFilter", urlPatterns = "/**")
public class EscapeExceptionFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (Throwable t) {
            ApiResponse<Void> response = ApiResponse.exception(t);
            servletResponse.getOutputStream().print(JSON.toJSONString(response));
            servletResponse.getOutputStream().flush();
        }
    }
}
