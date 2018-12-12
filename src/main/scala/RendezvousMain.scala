import akka.actor.{ActorSystem, Props}
import clients.test.TestActor
import com.typesafe.config.ConfigFactory
import rendezvous.RendezvousActor
import utils.Utils

object RendezvousMain extends App {
  override def main(args: Array[String]) = {

    val configuration = ConfigFactory.parseString(Utils.getConf("127.0.0.1","69"))



    val rendezvousSystem = ActorSystem("RemoteService", config = configuration)

    val rendezvousActor = rendezvousSystem.actorOf(Props(new RendezvousActor(args(0).toInt, args(1).toInt)), "rendezvous")


    //log.info(configuration)
    //log.info(s"${rendezvousActor}")
  }
}