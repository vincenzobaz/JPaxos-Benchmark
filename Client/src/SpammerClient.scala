import scala.util.Random
import lsr.paxos.client.{Client => PaxosClient}
import lsr.common.Configuration
import akka.http.scaladsl.Http
import akka.stream.ThrottleMode
import akka.stream.scaladsl.{Flow, Source}
import dummyservice.{Command, CommandType}
import tools.{AkkaConfig, NetworkStoppable}
import scala.concurrent.duration._

object SpammerClient extends App with AkkaConfig with NetworkStoppable {

  val localId = args(2).toInt

  // Listen to stop messages
  val bindingFuture = Http().bindAndHandle(stopRoute, "0.0.0.0", args(1).toInt)

  // PHASE 1: Start the client
  val paxosClient = new PaxosClient(new Configuration(args(0)))
  paxosClient.connect()

  // Classical Scala stream
  val requests: Stream[Command] = {
    val random = new Random()
    Stream.continually(random.nextBoolean())
      .map(isRead => if (isRead) new Command(CommandType.READ, -1) else new Command(CommandType.WRITE, random.nextInt))
  }

  //val rateLimiter = Flow[Command].throttle(100, 1 seconds, 0, ThrottleMode.Shaping)
  val slowerStream = Source(requests)//.via(rateLimiter)

  val logger = {
    if (args.length >= 4 && args(3) != "null") {
      println("Starting spy")
      new PuppetMasterSpy(slowerStream, paxosClient, localId, args(3))
    } else {
      println("Starting logger")
      new LogSpammer(slowerStream, paxosClient)
    }
  }
}
