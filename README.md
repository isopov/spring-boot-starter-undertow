This is a try to add [Undertow](http://undertow.io) servlet container option to the [spring-boot](http://projects.spring.io/spring-boot/) along the Jetty and Tomcat servlet containers.

Undertow is the basis of the [Wildfly](http://www.wildfly.org/) Application Server which is the basis of the Jboss Application Server and is based on the [XNIO](http://www.jboss.org/xnio) lowlevel I/O library.

To use it (it is now capable of serving only very simple hello world applications) add:
```
<dependency>
	<groupId>com.sopovs.moradanen.spring.boot.undertow</groupId>
	<artifactId>spring-boot-starter-undertow</artifactId>
	<version>${version}</version>
</dependency>
```

to the dependencies of your spring-boot project and 
```
<repository>
	<id>isopov-dintray</id>
	<name>isopov Bintray</name>
	<url>http://dl.bintray.com/isopov/maven/</url>
</repository>
```

to the repositories section.
