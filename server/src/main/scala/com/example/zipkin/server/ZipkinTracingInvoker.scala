package com.example.zipkin.server

import org.apache.cxf.jaxrs.JAXRSInvoker
import com.twitter.finagle.tracing.TraceId
import org.apache.cxf.message.Exchange
import com.twitter.finagle.tracing.Trace
import com.twitter.finagle.tracing.Annotation
import org.apache.cxf.jaxrs.model.OperationResourceInfo
import org.apache.cxf.jaxrs.ext.MessageContextImpl
import com.twitter.finagle.tracing.SpanId
import com.twitter.finagle.http.HttpTracing
import com.twitter.finagle.tracing.Flags
import scala.collection.JavaConversions._
import com.twitter.finagle.tracing.Tracer
import javax.inject.Inject

class ZipkinTracingInvoker extends JAXRSInvoker {
  @Inject val tracer: Tracer = null
  
  def trace[ R ]( exchange: Exchange )( block: => R ): R = {
    val context = new MessageContextImpl( exchange.getInMessage() )
    Trace.pushTracer( tracer )
        
    val id = Option( exchange.get( classOf[ OperationResourceInfo ] ) ) map { ori =>
      context.getHttpHeaders().getRequestHeader( HttpTracing.Header.SpanId ).toList match {
          case x :: xs => SpanId.fromString( x ) map { sid =>	
            val traceId = context.getHttpHeaders().getRequestHeader( HttpTracing.Header.TraceId ).toList match {
		      case x :: xs => SpanId.fromString( x )
		      case _ => None
		    }
		        
		    val parentSpanId = context.getHttpHeaders().getRequestHeader( HttpTracing.Header.ParentSpanId ).toList match {
		      case x :: xs => SpanId.fromString( x )
		      case _ => None
		    }
		
		    val sampled = context.getHttpHeaders().getRequestHeader( HttpTracing.Header.Sampled ).toList match { 
		      case x :: xs =>  x.toBoolean
		      case _ => true
		    }
		                  
		    val flags = context.getHttpHeaders().getRequestHeader( HttpTracing.Header.Flags ).toList match {
		      case x :: xs =>  Flags( x.toLong )
		      case _ => Flags()
		    }
	        
		    val id = TraceId( traceId, parentSpanId, sid, Option( sampled ), flags )          		         
		    Trace.setId( id )
		      
	        if( Trace.isActivelyTracing ) {
			    Trace.recordRpcname( context.getHttpServletRequest().getProtocol(), ori.getHttpMethod() )
			    Trace.record( Annotation.ServerRecv() )
			}
		      
		    id
	      }           
          
          case _ => None
      }
    }
    
    val result = block
    
    if( Trace.isActivelyTracing ) {
        id map { id => Trace.record( new Annotation.ServerSend() ) }
    }
    
    result

  }
  
  @Override
  override def invoke( exchange: Exchange, parametersList: AnyRef ): AnyRef = {
    trace( exchange )( super.invoke( exchange, parametersList ) )    	
  }
}