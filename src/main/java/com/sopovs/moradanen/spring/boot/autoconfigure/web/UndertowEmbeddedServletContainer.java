package com.sopovs.moradanen.spring.boot.autoconfigure.web;

import javax.servlet.ServletException;

import io.undertow.Undertow;
import io.undertow.Undertow.Builder;
import io.undertow.servlet.api.DeploymentManager;

import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerException;

public class UndertowEmbeddedServletContainer implements EmbeddedServletContainer {

    private final DeploymentManager manager;
    private final Builder builder;
    private final int port;
    private volatile Undertow undertow;
    // TODO is this safe? is it needed? Why spring-boot calls stop without
    // calling start before?
    private volatile boolean started = false;

    public UndertowEmbeddedServletContainer(Builder builder, DeploymentManager manager, int port) {
        this.builder = builder;
        this.manager = manager;
        this.port = port;
    }

    @Override
    public void start() throws EmbeddedServletContainerException {
        if (undertow == null) {
            try {
                undertow = builder.setHandler(manager.start()).build();
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