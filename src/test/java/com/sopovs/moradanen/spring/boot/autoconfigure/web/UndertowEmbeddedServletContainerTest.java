package com.sopovs.moradanen.spring.boot.autoconfigure.web;

import static io.undertow.servlet.Servlets.defaultContainer;
import static io.undertow.servlet.Servlets.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.undertow.Undertow;
import io.undertow.Undertow.Builder;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;

import org.junit.Test;

public class UndertowEmbeddedServletContainerTest {

    @Test
    public void testStartStopStartStop() {
        Builder builder = Undertow.builder().addHttpListener(8080, "localhost");
        DeploymentInfo deployment = Servlets.deployment()
                .setClassLoader(getClass().getClassLoader())
                .setContextPath("")
                .setDeploymentName("test.war")
                .addServlet(servlet("hello", HelloServlet.class).addMapping("/*"));
        DeploymentManager manager = defaultContainer().addDeployment(deployment);
        manager.deploy();

        UndertowEmbeddedServletContainer container = new UndertowEmbeddedServletContainer(builder, manager, "", 8080);
        container.start();
        container.stop();

        container.start();
        container.stop();
    }

    public static class HelloServlet extends HttpServlet {
        private static final long serialVersionUID = 1L;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.getWriter().println("Hello World!");
        }
    }
}
