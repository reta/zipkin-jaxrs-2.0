package com.example.zipkin

import java.util.concurrent.Callable
import com.twitter.finagle.stats.DefaultStatsReceiver
import com.twitter.finagle.tracing.Trace
import com.twitter.finagle.zipkin.thrift.ZipkinTracer
import com.twitter.finagle.tracing.Annotation

object Zipkin {
    lazy val tracer = ZipkinTracer.mk( host = "localhost", port = 9410, DefaultStatsReceiver, 1 )
    
    def invoke[ R ]( service: String, method: String, callable: Callable[ R ] ): R = Trace.unwind {
      Trace.pushTracerAndSetNextId( tracer, false )    		
      
      Trace.recordRpcname( service, method );
      Trace.record( new Annotation.ClientSend() );
    	    	
      try {
        callable.call()
      } finally {
  	    Trace.record( new Annotation.ClientRecv() );
      }
    }   
}