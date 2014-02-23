package com.example.zipkin.client

import javax.ws.rs.client.ClientResponseFilter
import javax.ws.rs.client.ClientRequestContext
import javax.ws.rs.client.ClientResponseContext
import javax.ws.rs.ext.Provider
import com.twitter.finagle.tracing.Trace
import com.twitter.finagle.tracing.Annotation
import com.twitter.finagle.tracing.SpanId
import com.twitter.finagle.http.HttpTracing
import com.twitter.finagle.tracing.TraceId
import com.twitter.finagle.tracing.Flags
import com.twitter.finagle.tracing.Tracer

@Provider
class ZipkinResponseFilter( val name: String, val tracer: Tracer ) extends ClientResponseFilter {
  
  def filter( requestContext: ClientRequestContext, responseContext: ClientResponseContext ): Unit = {
      val spanId = SpanId.fromString( requestContext.getHeaders().getFirst( HttpTracing.Header.SpanId ).toString() )

      spanId foreach { sid =>
        val traceId = SpanId.fromString( requestContext.getHeaders().getFirst( HttpTracing.Header.TraceId ).toString() )
        
        val parentSpanId = requestContext.getHeaders().getFirst( HttpTracing.Header.ParentSpanId ) match {
          case s: String => SpanId.fromString( s.toString() )
          case _ => None
        }

        val sampled = requestContext.getHeaders().getFirst( HttpTracing.Header.Sampled ) match { 
        	case s: String =>  s.toString.toBoolean
        	case _ => true
        }
        
        val flags = Flags( requestContext.getHeaders().getFirst( HttpTracing.Header.Flags ).toString.toLong )        
        Trace.setId( TraceId( traceId, parentSpanId, sid, Option( sampled ), flags ) )
      }
      
      if( Trace.isActivelyTracing ) {
    	Trace.record( Annotation.ClientRecv() )
      }
  }
}