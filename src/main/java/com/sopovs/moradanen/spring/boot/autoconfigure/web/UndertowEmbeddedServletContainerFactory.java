package com.sopovs.moradanen.spring.boot.autoconfigure.web;

import static io.undertow.servlet.Servlets.defaultContainer;
import static io.undertow.servlet.Servlets.deployment;
import static io.undertow.servlet.Servlets.servlet;
import io.undertow.Undertow;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.handlers.DefaultServlet;

import java.util.Arrays;

import org.springframework.boot.context.embedded.AbstractEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerException;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

public class UndertowEmbeddedServletContainerFactory extends
		AbstractEmbeddedServletContainerFactory implements ResourceLoaderAware {

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
	public EmbeddedServletContainer getEmbeddedServletContainer(ServletContextInitializer... initializers) {
		if (getPort() == 0) {
			return EmbeddedServletContainer.NONE;
		}

		DeploymentInfo servletBuilder = deployment();
		servletBuilder.setClassLoader(resourceLoader.getClassLoader());
		String contextPath = getContextPath();
		contextPath = StringUtils.hasLength(contextPath) ? contextPath : "/";
		servletBuilder.setContextPath(contextPath);
		// TODO
		servletBuilder.setDeploymentName(contextPath);
		if (isRegisterDefaultServlet()) {
			servletBuilder.addServlet(servlet("default", DefaultServlet.class));
		}
		if (isRegisterJspServlet()) {
			// TODO
		}

		// TODO
		getValidDocumentRoot();

		try {
			DeploymentManager manager = defaultContainer().addDeployment(servletBuilder);
			SpringBootServletExtension.initializers = Arrays.asList(initializers);
			manager.deploy();

			manager.getDeployment().getSessionManager().setDefaultSessionTimeout(getSessionTimeout());

			Undertow undertow = Undertow.builder()
					// TODO localhost or something else?
					.addListener(getPort(), "localhost")
					.setHandler(manager.start())
					.build();
			return new UndertowEmbeddedServletContainer(undertow);
		} catch (Exception ex) {
			throw new EmbeddedServletContainerException("Unable to start embdedded Undertow", ex);
		}
	}

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

}
