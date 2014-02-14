package com.sopovs.moradanen.spring.boot.autoconfigure.web;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.nio.charset.Charset;

import org.junit.After;
import org.junit.Test;
import org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

//Mostly copy&paste from EmbeddedServletContainerMvcIntegrationTests
public class EmbeddedUndertowMvcIntegrationTests {
    private AnnotationConfigEmbeddedWebApplicationContext context;

    @After
    public void closeContext() {
        try {
            this.context.close();
        } catch (Exception ex) {
        }
    }

    @Test
    public void undertow() throws Exception {
        this.context = new AnnotationConfigEmbeddedWebApplicationContext(
                UndertowEmbeddedServletContainerFactory.class, Config.class);
        doTest(this.context, "http://localhost:8080/hello");
    }

    private void doTest(AnnotationConfigEmbeddedWebApplicationContext context, String url)
            throws Exception {
        SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        ClientHttpRequest request = clientHttpRequestFactory.createRequest(new URI(url),
                HttpMethod.GET);
        ClientHttpResponse response = request.execute();
        try {
            String actual = StreamUtils.copyToString(response.getBody(), Charset.forName("UTF-8"));
            assertThat(actual, equalTo("Hello World"));
        } finally {
            response.close();
        }
    }

    @Configuration
    @EnableWebMvc
    public static class Config {

        @Bean
        public DispatcherServlet dispatcherServlet() {
            return new DispatcherServlet();
            // Alternatively you can use ServletContextInitializer beans
            // including
            // ServletRegistration and FilterRegistration. Read the
            // EmbeddedWebApplicationContext javadoc for details
        }

        @Bean
        public HelloWorldController helloWorldController() {
            return new HelloWorldController();
        }
    }

    @Controller
    public static class HelloWorldController {

        @RequestMapping("/hello")
        @ResponseBody
        public String sayHello() {
            return "Hello World";
        }
    }

}
