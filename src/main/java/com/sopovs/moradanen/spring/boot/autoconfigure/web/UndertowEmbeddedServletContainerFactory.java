package com.sopovs.moradanen.spring.boot.autoconfigure.web;

import static io.undertow.servlet.Servlets.defaultContainer;
import static io.undertow.servlet.Servlets.deployment;
import static io.undertow.servlet.Servlets.servlet;
import io.undertow.Undertow;
import io.undertow.Undertow.Builder;
import io.undertow.UndertowMessages;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.Resource;
import io.undertow.server.handlers.resource.ResourceChangeListener;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.server.handlers.resource.URLResource;
import io.undertow.servlet.api.DefaultServletConfig;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.InstanceFactory;
import io.undertow.servlet.api.InstanceHandle;
import io.undertow.servlet.api.ListenerInfo;
import io.undertow.servlet.api.MimeMapping;
import io.undertow.servlet.api.ServletStackTraces;
import io.undertow.servlet.handlers.DefaultServlet;
import io.undertow.servlet.util.ImmediateInstanceHandle;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.embedded.AbstractEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.ErrorPage;
import org.springframework.boot.context.embedded.MimeMappings.Mapping;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

/**
 * @author isopov
 */
public class UndertowEmbeddedServletContainerFactory extends
        AbstractEmbeddedServletContainerFactory implements ResourceLoaderAware, DisposableBean {

    private final Logger logger = LoggerFactory
            .getLogger(UndertowEmbeddedServletContainerFactory.class);

    private ResourceLoader resourceLoader;

    private File explodedWar;

    private Integer bufferSize;
    private Integer buffersPerRegion;
    private Integer ioThreads;
    private Integer workerThreads;
    private Boolean directBuffers;

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
    public EmbeddedServletContainer getEmbeddedServletContainer(ServletContextInitializer... initializers) {
        if (getPort() == 0) {
            return EmbeddedServletContainer.NONE;
        }

        DeploymentInfo servletBuilder = deployment();

        servletBuilder.addListener(
                new ListenerInfo(UndertowSpringServletContextListener.class,
                        new UndertowSpringServletContextListenerFactory(
                                new UndertowSpringServletContextListener(mergeInitializers(initializers)))));

        if (resourceLoader != null) {
            servletBuilder.setClassLoader(resourceLoader.getClassLoader());
        } else {
            servletBuilder.setClassLoader(getClass().getClassLoader());
        }
        servletBuilder.setContextPath(getContextPath());
        servletBuilder.setDeploymentName(getDeploymentName());
        if (isRegisterDefaultServlet()) {
            servletBuilder.addServlet(servlet("default", DefaultServlet.class));
            servletBuilder.setDefaultServletConfig(new DefaultServletConfig(true));
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
        servletBuilder.setServletStackTraces(ServletStackTraces.NONE);

        File root = getValidDocumentRoot();
        if (root != null && root.isDirectory()) {
            servletBuilder.setResourceManager(new FileResourceManager(getValidDocumentRoot(), 0));
        } else if (root != null && root.isFile()) {
            servletBuilder.setResourceManager(getJarResourceManager());
        } else if (resourceLoader != null) {
            // TODO is this needed?
            servletBuilder.setResourceManager(new ClassPathResourceManager(resourceLoader.getClassLoader(), ""));
        } else {
            // TODO is this needed?
            servletBuilder.setResourceManager(new ClassPathResourceManager(getClass().getClassLoader(), ""));
        }
        for (Mapping mimeMapping : getMimeMappings()) {
            servletBuilder.addMimeMapping(new MimeMapping(mimeMapping.getExtension(), mimeMapping.getMimeType()));
        }

        DeploymentManager manager = defaultContainer().addDeployment(servletBuilder);

        manager.deploy();

        manager.getDeployment().getSessionManager().setDefaultSessionTimeout(getSessionTimeout());

        Builder builder = Undertow.builder();
        if (bufferSize != null) {
            builder.setBufferSize(bufferSize);
        }
        if (buffersPerRegion != null) {
            builder.setBuffersPerRegion(buffersPerRegion);
        }
        if (ioThreads != null) {
            builder.setIoThreads(ioThreads);
        }
        if (workerThreads != null) {
            builder.setWorkerThreads(workerThreads);
        }
        if (directBuffers != null) {
            builder.setDirectBuffers(directBuffers);
        }

        // TODO localhost or something else?
        builder.addHttpListener(getPort(), "0.0.0.0");

        return new UndertowEmbeddedServletContainer(builder, manager, getContextPath(), getPort());

    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    private ResourceManager getJarResourceManager() {
        try {
            // TODO hack to be removed if JarResourceManager will be enough -
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

    public void setBufferSize(Integer bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setBuffersPerRegion(Integer buffersPerRegion) {
        this.buffersPerRegion = buffersPerRegion;
    }

    public void setIoThreads(Integer ioThreads) {
        this.ioThreads = ioThreads;
    }

    public void setWorkerThreads(Integer workerThreads) {
        this.workerThreads = workerThreads;
    }

    public void setDirectBuffers(Boolean directBuffers) {
        this.directBuffers = directBuffers;
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

    private static class UndertowSpringServletContextListenerFactory implements
            InstanceFactory<UndertowSpringServletContextListener> {

        private final UndertowSpringServletContextListener listener;

        public UndertowSpringServletContextListenerFactory(UndertowSpringServletContextListener listener) {
            this.listener = listener;
        }

        @Override
        public InstanceHandle<UndertowSpringServletContextListener> createInstance() throws InstantiationException {
            return new ImmediateInstanceHandle<UndertowSpringServletContextListener>(listener);
        }

    }

    private static class UndertowSpringServletContextListener implements ServletContextListener {
        private final ServletContextInitializer[] initializers;

        public UndertowSpringServletContextListener(ServletContextInitializer... initializers) {
            this.initializers = initializers;
        }

        @Override
        public void contextInitialized(ServletContextEvent sce) {
            try {
                for (ServletContextInitializer initializer : initializers) {
                    initializer.onStartup(sce.getServletContext());
                }
            } catch (ServletException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void contextDestroyed(ServletContextEvent sce) {
            // no code
        }
    }

}
