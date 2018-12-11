
import akka.actor.{ActorSystem, Props}
import clients.test.{StartTest, TestActor, Validate}
import clients.{ClientActor, InitClient}
import com.typesafe.config.ConfigFactory
import utils.Utils


object ClientMain extends App {
  override def main(args: Array[String]) = {
    val ip = args(0)
    val port = args(1)
    val configuration = ConfigFactory.parseString(Utils.getConf(ip, port))


    //println(configuration)

    val rendezvousIP = "127.0.0.1"
    val rendezvousPort = 69

    val clientActorSystem = ActorSystem("RemoteService", config = configuration)

    val testActor = clientActorSystem.actorOf(Props[TestActor], "testActor")
    val clientActor = clientActorSystem.actorOf(Props(new ClientActor(ip, port.toInt, rendezvousIP, rendezvousPort)), "clientActor")


    clientActor ! InitClient(-1, testActor) // -1 for random smr

    Thread.sleep(90*1000) //wait for response of rendezvous with the replicas

    testActor ! StartTest(clientActor, testDuration=20000) //processo de indentificacao

    Thread.sleep(60*1000)
    testActor ! Validate()
  }
}
