package client

import lpd.register.{Command, CommandType, Response}
import lsr.paxos.client.{Client => PaxosClient}
import lsr.common.Configuration

import scala.util.Random
import scala.concurrent.duration._

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}
import akka.stream.ThrottleMode

object SpammerClient extends App {
  // PHASE 1: Start all the replicas and the client
  val paxosClient = new PaxosClient(new Configuration(args(0)))
  paxosClient.connect()

  // Classical Scala stream
  val responses: Stream[Response] = {
    val random = new Random()
    Stream.continually(random.nextBoolean())
      .map(isRead => if (isRead) new Command(CommandType.READ, -1) else new Command(CommandType.WRITE, random.nextInt))
      .map(_.toByteArray)
      .map(paxosClient.execute(_))
      .map(new Response(_))
  }

  val source: Source[Response, NotUsed] = Source(responses)
  val modulator = Flow[Response].throttle(1, 1 seconds, 0, ThrottleMode.Shaping)
  source.via(modulator).runForeach(r => logger.info(s"Operation completed: ${r.toString}"))

}
