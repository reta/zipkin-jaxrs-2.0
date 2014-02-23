package com.example.server.config;

import java.util.Arrays;

import javax.ws.rs.ext.RuntimeDelegate;

import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import com.example.server.rs.JaxRsApiApplication;
import com.example.server.rs.PeopleRestService;
import com.example.zipkin.Zipkin;
import com.example.zipkin.server.ZipkinTracingInvoker;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.twitter.finagle.tracing.Tracer;

@Configuration
public class AppConfig {	
	@Bean( destroyMethod = "shutdown" )
	public SpringBus cxf() {
		return new SpringBus();
	}
	
	@Bean @DependsOn ( "cxf" )
	public Server jaxRsServer() {
		JAXRSServerFactoryBean factory = RuntimeDelegate.getInstance().createEndpoint( jaxRsApiApplication(), JAXRSServerFactoryBean.class );
		factory.setServiceBeans( Arrays.< Object >asList( peopleRestService() ) );
		factory.setAddress( factory.getAddress() );
		factory.setProviders( Arrays.< Object >asList( jsonProvider() ) );
		factory.setInvoker( zipkinTracingInvoker() );
		return factory.create();
	}
	
	@Bean
	public ZipkinTracingInvoker zipkinTracingInvoker() {
		return new ZipkinTracingInvoker();
	}
	
	@Bean
	public Tracer tracer() {
		return Zipkin.tracer();
	}
	
	@Bean 	
	public JaxRsApiApplication jaxRsApiApplication() {
		return new JaxRsApiApplication();
	}
	
	@Bean 
	public PeopleRestService peopleRestService() {
		return new PeopleRestService();
	}
		
	@Bean
	public JacksonJsonProvider jsonProvider() {
		return new JacksonJsonProvider();
	}
}
