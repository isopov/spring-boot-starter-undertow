package com.sopovs.moradanen.spring.boot.autoconfigure.web;

import static io.undertow.servlet.Servlets.defaultContainer;
import static io.undertow.servlet.Servlets.deployment;
import static io.undertow.servlet.Servlets.servlet;
import io.undertow.Undertow;
import io.undertow.UndertowMessages;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.Resource;
import io.undertow.server.handlers.resource.ResourceChangeListener;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.server.handlers.resource.URLResource;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.handlers.DefaultServlet;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.embedded.AbstractEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerException;
import org.springframework.boot.context.embedded.ErrorPage;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

public class UndertowEmbeddedServletContainerFactory extends
        AbstractEmbeddedServletContainerFactory implements ResourceLoaderAware, DisposableBean {

    private final Logger logger = LoggerFactory
            .getLogger(UndertowEmbeddedServletContainerFactory.class);

    private ResourceLoader resourceLoader;

    private File explodedWar;

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

    private String getDeploymentName() {
        return "TODO";
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
        servletBuilder.setDeploymentName(getDeploymentName());
        if (isRegisterDefaultServlet()) {
            servletBuilder.addServlet(servlet("default", DefaultServlet.class));
        }
        if (isRegisterJspServlet()) {
            logger.error("JSPs are not supported with Undertow");
        }
        for (ErrorPage springErrorPage : getErrorPages()) {
            if (springErrorPage.getStatus() != null) {
                io.undertow.servlet.api.ErrorPage undertowErrorpage =
                        new io.undertow.servlet.api.ErrorPage(springErrorPage.getPath(),
                                springErrorPage.getStatusCode());
                servletBuilder.addErrorPage(undertowErrorpage);
            } else if (springErrorPage.getException() != null) {
                io.undertow.servlet.api.ErrorPage undertowErrorpage =
                        new io.undertow.servlet.api.ErrorPage(springErrorPage.getPath(),
                                springErrorPage.getException());
                servletBuilder.addErrorPage(undertowErrorpage);
            } else {
                // TODO how is this supposed to work?
                io.undertow.servlet.api.ErrorPage undertowErrorpage =
                        new io.undertow.servlet.api.ErrorPage(springErrorPage.getPath());
                servletBuilder.addErrorPage(undertowErrorpage);
            }

        }
        File root = getValidDocumentRoot();
        if (root != null && root.isDirectory()) {
            servletBuilder.setResourceManager(new FileResourceManager(getValidDocumentRoot(), 0));
        } else if (root != null && root.isFile()) {
            servletBuilder.setResourceManager(getJarResourceManager());
        } else {
            // TODO is this needed?
            servletBuilder.setResourceManager(new ClassPathResourceManager(resourceLoader.getClassLoader(), ""));
        }
        try {
            DeploymentManager manager = defaultContainer().addDeployment(servletBuilder);
            SpringBootServletExtension.initializers = Arrays.asList(initializers);
            manager.deploy();

            manager.getDeployment().getSessionManager().setDefaultSessionTimeout(getSessionTimeout());

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

    private ResourceManager getJarResourceManager() {
        try {
            // TODO hack to be rem,oved if JarResourceManager will be enough -
            // or move option to perform this hack to configuration
            explodedWar = File.createTempFile(getDeploymentName(), "-boot");
            explodedWar.delete();
            explodedWar.mkdir();
            ZipFile zipFile = new ZipFile(getValidDocumentRoot());
            zipFile.extractAll(explodedWar.getAbsolutePath());
            return new FileResourceManager(explodedWar, 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ZipException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unused")
    // Freemarker views are working this way but can not load taglibs from other
    // jars
    private static class JarResourcemanager implements ResourceManager {
        private final String jarPath;

        public JarResourcemanager(File jarFile) {
            this(jarFile.getAbsolutePath());
        }

        public JarResourcemanager(String jarPath) {
            this.jarPath = jarPath;
        }

        @Override
        public void close() throws IOException {
            // no code
        }

        @Override
        public Resource getResource(String path) throws IOException {
            URL url = new URL("jar:file:" + jarPath + "!" + path);
            URLResource resource = new URLResource(url, url.openConnection(), path);
            if (resource.getContentLength() < 0) {
                return null;
            }
            return resource;
        }

        @Override
        public boolean isResourceChangeListenerSupported() {
            return false;
        }

        @Override
        public void registerResourceChangeListener(ResourceChangeListener listener) {
            throw UndertowMessages.MESSAGES.resourceChangeListenerNotSupported();

        }

        @Override
        public void removeResourceChangeListener(ResourceChangeListener listener) {
            throw UndertowMessages.MESSAGES.resourceChangeListenerNotSupported();

        }

    }

    @Override
    public void destroy() throws Exception {
        if (explodedWar != null && explodedWar.isDirectory()) {
            deleteFolder(explodedWar);
        }
    }

    private static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }
}
