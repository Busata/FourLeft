package io.busata.fourleft.infrastructure.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@ParametersAreNonnullByDefault
@Component
public class SlowEndpointsInterceptor implements HandlerInterceptor {

    public int THRESHOLD_SLOW_CALL = 1500;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
            long startTime = (Long) request.getAttribute("startTime");
            long endTime = System.currentTimeMillis();
            long executeTime = endTime - startTime;

            if(executeTime > THRESHOLD_SLOW_CALL) {
                log.info("SlowEndpoint: {} milliseconds. Request URL: {}",  executeTime, request.getRequestURI());
            }
    }
}
