package com.example.server.rs;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.example.model.Person;
import com.example.zipkin.Zipkin;

@Path( "/people" ) 
public class PeopleRestService {
	@Produces( { "application/json" } )
	@GET
	public Collection< Person > getPeople() {
		return Zipkin.invoke( "DB", "FIND ALL", new Callable< Collection< Person > >() {
			@Override
			public Collection<Person> call() throws Exception {
				return Arrays.asList( new Person( "Tom", "Bombdil" ) );
			}			
		} ); 		
	}
}
