import scala.util.Random
import scala.concurrent.duration._
import lpd.register.{Command, CommandType, Response}
import lsr.paxos.client.{Client => PaxosClient}
import lsr.common.Configuration
import akka.NotUsed
import akka.http.scaladsl.Http
import akka.stream.scaladsl.{Flow, Source}
import akka.stream.ThrottleMode
import org.slf4j.{Logger, LoggerFactory}

object SpammerClient extends App with AkkaConfig with NetworkStoppable {
 // needed for the future flatMap/onComplete in the end
  //implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  val logger: Logger = LoggerFactory.getLogger(classOf[PaxosClient])

  // PHASE 1: Start the client
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

  val bindingFuture = Http().bindAndHandle(stopRoute, "localhost", args(1).toInt)
  val source: Source[Response, NotUsed] = Source(responses)
  val modulator = Flow[Response].throttle(1, 1 seconds, 0, ThrottleMode.Shaping)
  source.via(modulator).runForeach(r => logger.info(s"Operation completed: ${r.toString}"))
}
