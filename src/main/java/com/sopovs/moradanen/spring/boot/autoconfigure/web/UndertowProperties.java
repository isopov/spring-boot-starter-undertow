package com.sopovs.moradanen.spring.boot.autoconfigure.web;

import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(name = "undertow", ignoreUnknownFields = false)
public class UndertowProperties implements EmbeddedServletContainerCustomizer {

    private Integer bufferSize;
    private Integer buffersPerRegion;
    private Integer ioThreads;
    private Integer workerThreads;
    private Boolean directBuffers;

    @Override
    public void customize(ConfigurableEmbeddedServletContainerFactory factory) {
        UndertowEmbeddedServletContainerFactory undertowFactory = (UndertowEmbeddedServletContainerFactory) factory;
        undertowFactory.setBufferSize(bufferSize);
        undertowFactory.setBuffersPerRegion(buffersPerRegion);
        undertowFactory.setIoThreads(ioThreads);
        undertowFactory.setWorkerThreads(workerThreads);
        undertowFactory.setDirectBuffers(directBuffers);
    }

    public Integer getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(Integer bufferSize) {
        this.bufferSize = bufferSize;
    }

    public Integer getBuffersPerRegion() {
        return buffersPerRegion;
    }

    public void setBuffersPerRegion(Integer buffersPerRegion) {
        this.buffersPerRegion = buffersPerRegion;
    }

    public Integer getIoThreads() {
        return ioThreads;
    }

    public void setIoThreads(Integer ioThreads) {
        this.ioThreads = ioThreads;
    }

    public Integer getWorkerThreads() {
        return workerThreads;
    }

    public void setWorkerThreads(Integer workerThreads) {
        this.workerThreads = workerThreads;
    }

    public Boolean getDirectBuffers() {
        return directBuffers;
    }

    public void setDirectBuffers(Boolean directBuffers) {
        this.directBuffers = directBuffers;
    }

}
