package rendezvous

import akka.actor.ActorRef
import utils.ReplicaNode


case class IdentifySmr(node: ReplicaNode)

case class IdentifyClient(client: ActorRef)

case class GetClient(node: ActorRef)