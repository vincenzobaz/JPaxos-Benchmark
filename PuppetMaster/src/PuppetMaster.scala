import akka.NotUsed
import akka.actor.Cancellable
import akka.http.scaladsl.Http
import akka.stream.ThrottleMode
import akka.stream.scaladsl.{Flow, Source}
import akka.pattern

import lpd.register.{Command, CommandType, Response}
import lsr.paxos.client.{Client => PaxosClient}

import scala.concurrent.Future
import scala.io.StdIn
import scala.concurrent.duration._

import org.slf4j.{Logger, LoggerFactory}


object PuppetMaster extends App with AkkaConfig {

  val receptionist = new Receptionist(args.drop(1))

  // PHASE 1: Start all the replicas
  receptionist.remotes.foreach(_.start)

  pattern.after(5 seconds, system.scheduler)(Future { receptionist.remotes(0).kill })
  //Source.tick(0 seconds, 10 seconds, ()).runForeach(_ => receptionist.remotes(0).kill)
  //Source.tick(15 seconds, 5 seconds, ()).runForeach(_ => if (receptionist.remotes(0).isUp) receptionist.remotes(0).start)

  Http().bindAndHandle(receptionist.route, "localhost", 9090)
  StdIn.readLine()

  receptionist.remotes.foreach(_.stop)
}
