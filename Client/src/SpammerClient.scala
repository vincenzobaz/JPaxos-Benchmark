import scala.util.Random
import lpd.register.{Command, CommandType}
import lsr.paxos.client.{Client => PaxosClient}
import lsr.common.Configuration
import akka.http.scaladsl.Http

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

  val logger = {
    if (args.length >= 4 && args(3) != "null") {
      println("Starting spy")
      new PuppetMasterSpy(requests, paxosClient, localId, args(3))
    } else {
      println("Starting logger")
      new LogSpammer(requests, paxosClient)
    }
  }
}
