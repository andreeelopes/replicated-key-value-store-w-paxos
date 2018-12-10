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
      if (membership.size == numberOfReplicas) {
        //println(s"membership: $membership")
        membership.foreach { member => member.smrActor ! UpdateReplicas(membership) }
        // println(s"Sending membership to $member")}
        clients.foreach { client => client ! UpdateReplicas(membership) }
        //println(s"Sending membership to $client")}
      }

    case IdentifyClient(client: ActorRef) =>
      //println(s"Client= $client | path ${client.path}")
      clients += client
  }
}
