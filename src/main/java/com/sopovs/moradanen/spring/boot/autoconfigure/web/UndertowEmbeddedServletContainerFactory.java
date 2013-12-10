package com.sopovs.moradanen.spring.boot.autoconfigure.web;

import static io.undertow.servlet.Servlets.defaultContainer;
import static io.undertow.servlet.Servlets.deployment;
import static io.undertow.servlet.Servlets.servlet;
import io.undertow.Undertow;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.handlers.DefaultServlet;

import java.io.File;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.embedded.AbstractEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerException;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

public class UndertowEmbeddedServletContainerFactory extends
		AbstractEmbeddedServletContainerFactory implements ResourceLoaderAware {

	private final Logger logger = LoggerFactory
			.getLogger(UndertowEmbeddedServletContainerFactory.class);

	private ResourceLoader resourceLoader;

	/**
	 * Create a new {@link UndertowEmbeddedServletContainerFactory} instance.
	 */
	public UndertowEmbeddedServletContainerFactory() {
		super();
	}

	/**
	 * Create a new {@link UndertowEmbeddedServletContainerFactory} that listens
	 * for requests using the specified port.
	 * 
	 * @param port
	 *            the port to listen on
	 */
	public UndertowEmbeddedServletContainerFactory(int port) {
		super(port);
	}

	/**
	 * Create a new {@link UndertowEmbeddedServletContainerFactory} with the
	 * specified context path and port.
	 * 
	 * @param contextPath
	 *            root the context path
	 * @param port
	 *            the port to listen on
	 */
	public UndertowEmbeddedServletContainerFactory(String contextPath, int port) {
		super(contextPath, port);
	}

	@Override
	public EmbeddedServletContainer getEmbeddedServletContainer(
			ServletContextInitializer... initializers) {
		if (getPort() == 0) {
			return EmbeddedServletContainer.NONE;
		}

		DeploymentInfo servletBuilder = deployment();
		servletBuilder.setClassLoader(resourceLoader.getClassLoader());
		servletBuilder.setContextPath(getContextPath());
		servletBuilder.setDeploymentName("TODO");
		if (isRegisterDefaultServlet()) {
			servletBuilder.addServlet(servlet("default", DefaultServlet.class));
		}
		if (isRegisterJspServlet()) {
			logger.error("JSPs are not supported with Undertow");
		}
		File root = getValidDocumentRoot();
		if (root != null) {
			servletBuilder.setResourceManager(new FileResourceManager(
					getValidDocumentRoot(), 0));
		} else {
			// TODO is this needed?
			servletBuilder.setResourceManager(new ClassPathResourceManager(
					resourceLoader.getClassLoader(), ""));
		}
		try {
			DeploymentManager manager = defaultContainer().addDeployment(
					servletBuilder);
			SpringBootServletExtension.initializers = Arrays
					.asList(initializers);
			manager.deploy();

			manager.getDeployment().getSessionManager()
					.setDefaultSessionTimeout(getSessionTimeout());

			Undertow undertow = Undertow.builder()
					// TODO localhost or something else?
					.addListener(getPort(), "localhost")
					.setHandler(manager.start()).build();
			return new UndertowEmbeddedServletContainer(undertow);
		} catch (Exception ex) {
			throw new EmbeddedServletContainerException(
					"Unable to start embdedded Undertow", ex);
		}
	}

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}
}
