package rendezvous

import akka.actor.{Actor, ActorLogging, ActorRef}
import replicas.statemachinereplication.UpdateReplicas
import utils.ReplicaNode

class RendezvousActor(numberOfReplicas: Int) extends Actor with ActorLogging {

  var clients = Set[ActorRef]()
  var membership = Set[ReplicaNode]()


  override def receive: Receive = {
    case identification: IdentifySmr =>
      membership += identification.node
      if(membership.size == numberOfReplicas){
        membership.foreach(member => member.smrActor ! UpdateReplicas(membership))
        clients.foreach(client => client ! UpdateReplicas(membership))
      }

    case "IdentifyClient" =>
      clients += sender()
  }
}
