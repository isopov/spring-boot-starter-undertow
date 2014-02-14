package com.sopovs.moradanen.spring.boot.autoconfigure.web;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.boot.context.embedded.AbstractEmbeddedServletContainerFactoryTests;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.ErrorPage;
import org.springframework.boot.context.embedded.ExampleServlet;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.http.HttpStatus;

public class UndertowEmbeddedServletContainerFactoryTests extends
        AbstractEmbeddedServletContainerFactoryTests
{

    @Override
    protected UndertowEmbeddedServletContainerFactory getFactory()
    {
        return new UndertowEmbeddedServletContainerFactory();
    }

    @Override
    @Ignore
    @Test
    // Wait for the https://issues.jboss.org/browse/UNDERTOW-192
    public void errorPage() throws Exception {
        super.errorPage();
    }

    @Override
    @Ignore
    @Test
    // Wait for the https://issues.jboss.org/browse/UNDERTOW-193
    public void specificContextRoot() throws Exception {
        super.specificContextRoot();
    }

    @Test
    public void errorPage404() throws Exception {
        ConfigurableEmbeddedServletContainerFactory factory = getFactory();
        factory.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND, "/hello"));
        this.container = factory.getEmbeddedServletContainer(
                new ServletRegistrationBean(new ExampleServlet(), "/hello"));
        this.container.start();
        assertThat(getResponse("http://localhost:8080/hello"), equalTo("Hello World"));
        assertThat(getResponse("http://localhost:8080/not-found"), equalTo("Hello World"));
    }

    @Override
    @Ignore
    @Test
    public void mimeType() throws Exception {
        // TODO
        super.mimeType();
    }
}
