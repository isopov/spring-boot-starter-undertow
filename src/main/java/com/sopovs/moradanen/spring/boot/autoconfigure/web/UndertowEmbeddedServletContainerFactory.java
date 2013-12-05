package com.sopovs.moradanen.spring.boot.autoconfigure.web;

import static io.undertow.servlet.Servlets.defaultContainer;
import static io.undertow.servlet.Servlets.deployment;
import static io.undertow.servlet.Servlets.servlet;
import io.undertow.Undertow;
import io.undertow.jsp.HackInstanceManager;
import io.undertow.jsp.JspServletBuilder;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.handlers.DefaultServlet;

import java.util.Arrays;
import java.util.HashMap;

import org.apache.jasper.deploy.JspPropertyGroup;
import org.apache.jasper.deploy.TagLibraryInfo;
import org.springframework.boot.context.embedded.AbstractEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerException;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ClassUtils;
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
	public EmbeddedServletContainer getEmbeddedServletContainer(
			ServletContextInitializer... initializers) {
		if (getPort() == 0) {
			return EmbeddedServletContainer.NONE;
		}

		DeploymentInfo servletBuilder = deployment();
		servletBuilder.setClassLoader(resourceLoader.getClassLoader());
		String contextPath = getContextPath();
		contextPath = StringUtils.hasLength(contextPath) ? contextPath : "/";
		servletBuilder.setContextPath(contextPath);
		servletBuilder.setDeploymentName(contextPath);
		if (isRegisterDefaultServlet()) {
			servletBuilder.addServlet(servlet("default", DefaultServlet.class));
		}
		// TODO FIXME spring taglib is not usable
		if (isRegisterJspServlet()
				&& ClassUtils.isPresent("io.undertow.jsp.JspServletBuilder",
						getClass().getClassLoader())) {
			JspServletFactory.addJspServlet(servletBuilder);
		}
		servletBuilder.setResourceManager(new FileResourceManager(
				getValidDocumentRoot(), 0));

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

	private static class JspServletFactory {
		public static void addJspServlet(DeploymentInfo servletBuilder) {

			servletBuilder.addServlet(JspServletBuilder.createServlet("jsp",
					"*.jsp"));
			JspServletBuilder.setupDeployment(servletBuilder,
					new HashMap<String, JspPropertyGroup>(),
					new HashMap<String, TagLibraryInfo>(),
					new HackInstanceManager());
		}

	}

}
