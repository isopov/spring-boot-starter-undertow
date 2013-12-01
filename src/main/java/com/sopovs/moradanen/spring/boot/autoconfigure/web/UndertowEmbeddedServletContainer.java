package com.sopovs.moradanen.spring.boot.autoconfigure.web;

import io.undertow.Undertow;

import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerException;

public class UndertowEmbeddedServletContainer implements EmbeddedServletContainer {

	private final Undertow undertow;

	public UndertowEmbeddedServletContainer(Undertow undertow) {
		this.undertow = undertow;
	}

	@Override
	public void start() throws EmbeddedServletContainerException {
		undertow.start();

	}

	@Override
	public void stop() throws EmbeddedServletContainerException {
		undertow.stop();

	}

}