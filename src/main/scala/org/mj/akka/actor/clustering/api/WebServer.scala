package org.mj.akka.actor.clustering.api

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.io.StdIn

object WebServer extends App {
  implicit val system = ActorSystem("mj")
  implicit val ex = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val route =
    path("hello" / Segment) { id =>
      get {
        complete(HttpEntity(ContentTypes.`application/json`, s"Hello $id"))
      }
    }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
  val http =Http().bind("localhost", 8080)


  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done
}