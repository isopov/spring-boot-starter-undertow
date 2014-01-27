package com.sopovs.moradanen.spring.boot.autoconfigure.web;

import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Configuration
@EnableConfigurationProperties
@ConditionalOnWebApplication
public class UndertowPropertiesAutoConfiguration implements ApplicationContextAware,
        EmbeddedServletContainerCustomizer {

    private ApplicationContext applicationContext;

    @Bean(name = "com.sopovs.moradanen.spring.boot.autoconfigure.web.UndertowProperties")
    @ConditionalOnMissingBean(search = SearchStrategy.CURRENT)
    public UndertowProperties serverProperties() {
        return new UndertowProperties();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void customize(ConfigurableEmbeddedServletContainerFactory factory) {
        String[] undertowPropertiesBeans = this.applicationContext.getBeanNamesForType(UndertowProperties.class);
        Assert.state(undertowPropertiesBeans.length == 1, "Multiple UndertowProperties beans registered "
                + StringUtils.arrayToCommaDelimitedString(undertowPropertiesBeans));
    }

}