package com.sopovs.moradanen.spring.boot.autoconfigure.web;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.boot.context.embedded.AbstractEmbeddedServletContainerFactoryTests;

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
    public void mimeType() throws Exception {
        // TODO
        super.mimeType();
    }

    @Override
    @Ignore
    @Test
    public void multipleConfigurations() throws Exception {
        // TODO
        super.multipleConfigurations();
    }

    @Override
    @Ignore
    @Test
    public void loadOnStartAfterContextIsInitialized() throws Exception {
        // TODO
        super.loadOnStartAfterContextIsInitialized();
    }

    @Override
    @Ignore
    @Test
    public void specificContextRoot() throws Exception {
        // TODO
        super.specificContextRoot();
    }

}
