import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseKeys
import net.virtualvoid.sbt.graph.Plugin._

object ProjectBuild extends Build {
  lazy val defaultSettings =  Defaults.defaultSettings ++ graphSettings ++ assemblySettings ++ Seq(
      organization := "com.example",
      version := "0.0.1-SNAPSHOT",
      
      scalaVersion := "2.10.3",      
      crossPaths := false,
      autoScalaLibrary := false,
      
      EclipseKeys.withSource := true,
        
      scalacOptions ++= Seq( "-encoding", "UTF-8", "-target:jvm-1.7" ),    
      javacOptions in compile ++= Seq( "-encoding", "UTF-8", "-source", "1.7", "-target", "1.7" ),        
      javacOptions in doc ++= Seq( "-encoding", "UTF-8", "-source", "1.7" ),
      outputStrategy := Some( StdoutOutput ),            
        
      resolvers ++= Seq( 
          Resolver.mavenLocal, 
          Resolver.sonatypeRepo( "releases" ), 
          Resolver.typesafeRepo( "releases" ),
          "java.net" at "http://download.java.net/maven/2/"
      ),        
      
      compileOrder := CompileOrder.ScalaThenJava,
      
      libraryDependencies ++= Seq(
        "com.twitter" %% "finagle-zipkin" % "6.12.1" withSources(),
        "com.twitter" %% "finagle-http" % "6.12.1" withSources()
      )
  )
  
  lazy val server = Project( 
      id = "server",  
      base = file("server"),
      settings = defaultSettings ++ Seq(           
        name := "zipkin-jaxrs-2.0-server",           
                
//        excludedJars in assembly <<= (fullClasspath in assembly) map { cp => 
//            cp filter {_.data.getName == "commons-logging-1.1.1.jar"}
//        },
        
        mergeStrategy in assembly <<= (mergeStrategy in assembly) { old => {
            case PathList(ps @ _*) if List( "DEPENDENCIES", "LICENSE", "NOTICE" ).exists( ps.last equals _ ) => MergeStrategy.discard
            case PathList(ps @ _*) if List( "bus-extensions.txt" ).exists( ps.last equals _ ) => MergeStrategy.concat
            case PathList(ps @ _*) if List( ".MF", ".SF", ".DSA", ".RSA", ".html", ".txt" ).exists( ps.last endsWith _ ) => MergeStrategy.discard
            case PathList(ps @ _*) if ps.last contains "spring" => MergeStrategy.first
            case _ => MergeStrategy.deduplicate
          }  
        },
                
        mainClass := Some( "com.example.server.ServerStarter" ),
        mainClass in assembly := Some("com.example.server.ServerStarter"),
        mainClass in run := Some("com.example.server.ServerStarter"),
            
        libraryDependencies ++= Seq(
            "org.springframework" % "spring-core" % "4.0.1.RELEASE",
            "org.springframework" % "spring-context" % "4.0.1.RELEASE",
            "org.springframework" % "spring-web" % "4.0.1.RELEASE",
            "com.fasterxml.jackson.jaxrs" % "jackson-jaxrs-json-provider" % "2.2.3",
            "ch.qos.logback" % "logback-classic" % "1.1.1",
            "org.eclipse.jetty" % "jetty-server" % "9.1.1.v20140108",
            "org.eclipse.jetty" % "jetty-webapp" % "9.1.1.v20140108",
            "org.apache.cxf" % "cxf-rt-frontend-jaxrs" % "2.7.10"
        )
      )
   )

   lazy val client = Project(
      id = "client",
      base = file("client"),
      settings = defaultSettings ++ Seq(
        name := "zipkin-jaxrs-2.0-client",
        mainClass := Some( "com.example.client.ClientStarter" ),
        mainClass in assembly := Some("com.example.client.ClientStarter"),
        mainClass in run := Some("com.example.client.ClientStarter"),
        
        libraryDependencies ++= Seq(
            "org.glassfish.hk2" % "hk2-utils" % "2.2.0-b21",
            "org.glassfish.hk2" % "hk2-locator" % "2.2.0-b21",
            "org.glassfish.jersey.core" % "jersey-client" % "2.5.1" 
                exclude( "org.glassfish.hk2.external", "javax.inject" ) 
                exclude( "org.glassfish.hk2", "hk2-utils")
                exclude( "org.glassfish.hk2", "hk2-locator"),
            "javax.ws.rs" % "javax.ws.rs-api" % "2.0"            
        )
      )
   )

   lazy val root =  Project(
      id = "main",
      base = file( "." ) 
   ) aggregate( server, client )
}
