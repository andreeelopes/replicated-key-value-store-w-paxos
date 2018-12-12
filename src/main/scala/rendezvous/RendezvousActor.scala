package rendezvous

import akka.actor.{Actor, ActorLogging, ActorRef}
import replicas.statemachinereplication.UpdateReplicas
import utils.ReplicaNode

class RendezvousActor(numberOfReplicas: Int, numberOfClients: Int) extends Actor with ActorLogging {

  var clients = Set[ActorRef]()
  var membership = Set[ReplicaNode]()


  override def receive: Receive = {
    case identification: IdentifySmr =>
      membership += identification.node
      if (membership.size + clients.size == numberOfReplicas + numberOfClients) {
        log.info(s"membership: $membership")
        membership.foreach { member => member.smrActor ! UpdateReplicas(membership) }
        // println(s"Sending membership to $member")}
        clients.foreach { client => client ! UpdateReplicas(membership) }
        //println(s"Sending membership to $client")}
      }

    case IdentifyClient(client: ActorRef) =>
      log.info(s"Client= $client | path ${client.path}")
      clients += client

      if (membership.size + clients.size == numberOfReplicas + numberOfClients) {
        log.info(s"membership: $membership")
        membership.foreach { member => member.smrActor ! UpdateReplicas(membership) }
        // println(s"Sending membership to $member")}
        clients.foreach { client => client ! UpdateReplicas(membership) }
        //println(s"Sending membership to $client")}
      }

    case g: GetClient =>
      sender ! GetClient(clients.head)


  }
}
