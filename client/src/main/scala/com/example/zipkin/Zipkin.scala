package com.example.zipkin

import com.twitter.finagle.stats.DefaultStatsReceiver
import com.twitter.finagle.zipkin.thrift.ZipkinTracer
import com.twitter.finagle.tracing.Trace
import javax.ws.rs.ext.Provider

object Zipkin {
    lazy val tracer = ZipkinTracer.mk( host = "localhost", port = 9410, DefaultStatsReceiver, 1 )
}