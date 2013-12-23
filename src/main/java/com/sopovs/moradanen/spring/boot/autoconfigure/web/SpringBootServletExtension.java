package com.sopovs.moradanen.spring.boot.autoconfigure.web;

import io.undertow.servlet.ServletExtension;
import io.undertow.servlet.api.DeploymentInfo;

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.boot.context.embedded.ServletContextInitializer;

public class SpringBootServletExtension implements ServletExtension {

    static List<ServletContextInitializer> initializers;

    @Override
    public void handleDeployment(DeploymentInfo deploymentInfo, ServletContext servletContext) {
        try {
            for (ServletContextInitializer initializer : initializers) {
                initializer.onStartup(servletContext);
            }
        } catch (ServletException e) {
            // TODO
            throw new RuntimeException(e);
        } finally {
            initializers = null;
        }
    }

}
