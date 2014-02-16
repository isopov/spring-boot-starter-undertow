package com.sopovs.moradanen.spring.boot.autoconfigure.web;

import javax.servlet.ServletException;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.Undertow.Builder;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentManager;

import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerException;
import org.springframework.util.StringUtils;

public class UndertowEmbeddedServletContainer implements EmbeddedServletContainer {

    private final DeploymentManager manager;
    private final Builder builder;
    private final String contextPath;
    private final int port;
    private Undertow undertow;
    private boolean started = false;

    public UndertowEmbeddedServletContainer(Builder builder, DeploymentManager manager, String contextPath, int port) {
        this.builder = builder;
        this.manager = manager;
        this.contextPath = contextPath;
        this.port = port;
    }

    @Override
    public synchronized void start() throws EmbeddedServletContainerException {
        if (undertow == null) {
            try {
                HttpHandler servletHandler = manager.start();
                if (StringUtils.isEmpty(contextPath)) {
                    builder.setHandler(servletHandler);
                } else {
                    PathHandler pathHandler = Handlers.path().addPrefixPath(contextPath, servletHandler);
                    builder.setHandler(pathHandler);
                }
                undertow = builder.build();
            } catch (ServletException ex) {
                throw new EmbeddedServletContainerException("Unable to start embdedded Undertow", ex);
            }
        }
        undertow.start();
        started = true;
    }

    @Override
    public synchronized void stop() throws EmbeddedServletContainerException {
        if (started) {
            started = false;
            undertow.stop();
        }
    }

    @Override
    public int getPort() {
        return port;
    }
}