package com.example.client;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.example.zipkin.Zipkin;
import com.example.zipkin.client.ZipkinRequestFilter;
import com.example.zipkin.client.ZipkinResponseFilter;

public class ClientStarter {
	public static void main( final String[] args ) throws Exception {	
        final Client client = ClientBuilder
		    .newClient()
			.register( new ZipkinRequestFilter( "People", Zipkin.tracer() ), 1 )
			.register( new ZipkinResponseFilter( "People", Zipkin.tracer() ), 1 );        
                        
        final Response response = client
            .target( "http://localhost:8080/rest/api/people" )
            .request( MediaType.APPLICATION_JSON )
            .get();

        if( response.getStatus() == 200 ) {
        	System.out.println( response.readEntity( String.class ) );
        }
        
        response.close();
        client.close();
        
        // Small delay to allow tracer to send the trace over the wire
        Thread.sleep( 1000 );
	}
}
