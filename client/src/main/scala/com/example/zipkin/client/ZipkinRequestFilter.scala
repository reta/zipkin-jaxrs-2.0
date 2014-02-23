package com.example.zipkin.client

import javax.ws.rs.client.ClientRequestFilter
import javax.ws.rs.ext.Provider
import javax.ws.rs.client.ClientRequestContext
import com.twitter.finagle.http.HttpTracing
import com.twitter.finagle.tracing.Trace
import com.twitter.finagle.tracing.Annotation
import com.twitter.finagle.tracing.TraceId
import com.twitter.finagle.tracing.Tracer

@Provider
class ZipkinRequestFilter( val name: String, val tracer: Tracer ) extends ClientRequestFilter {
  def filter( requestContext: ClientRequestContext ): Unit = {      
    Trace.pushTracerAndSetNextId( tracer, true )
	
    requestContext.getHeaders().add( HttpTracing.Header.TraceId, Trace.id.traceId.toString )
	requestContext.getHeaders().add( HttpTracing.Header.SpanId, Trace.id.spanId.toString )
	        
	Trace.id._parentId foreach { id => requestContext.getHeaders().add( HttpTracing.Header.ParentSpanId, id.toString ) }    
	Trace.id.sampled foreach { sampled => requestContext.getHeaders().add( HttpTracing.Header.Sampled, sampled.toString ) }
	requestContext.getHeaders().add( HttpTracing.Header.Flags, Trace.id.flags.toLong.toString )
	            
	if( Trace.isActivelyTracing ) {
	  Trace.recordRpcname( name,  requestContext.getMethod() )
      Trace.recordBinary( "http.uri", requestContext.getUri().toString()  )
	  Trace.record( Annotation.ClientSend() )	 	 
	}
  }
}
