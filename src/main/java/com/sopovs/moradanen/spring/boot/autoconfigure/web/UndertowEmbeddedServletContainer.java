package com.sopovs.moradanen.spring.boot.autoconfigure.web;

import io.undertow.Undertow;

import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerException;

public class UndertowEmbeddedServletContainer implements EmbeddedServletContainer {

    private final Undertow undertow;
    private final int port;
    // TODO is this safe? is it needed? Why spring-boot calls stop without
    // calling start before?
    private volatile boolean started = false;

    public UndertowEmbeddedServletContainer(Undertow undertow, int port) {
        this.undertow = undertow;
        this.port = port;
    }

    @Override
    public void start() throws EmbeddedServletContainerException {
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