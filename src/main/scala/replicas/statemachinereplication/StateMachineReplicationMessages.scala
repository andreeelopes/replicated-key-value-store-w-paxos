package replicas.statemachinereplication

import akka.actor.ActorRef
import utils.ReplicaNode

import scala.collection.immutable.Queue

case class UpdateReplicas(replicas: Set[ReplicaNode])

case class Event(op: Operation, mid: String, sender: ActorRef, replica: ReplicaNode,
                 var executed: Boolean = false, var returnValue: String = null) {
  override def toString = s"Event($op, $mid, ${sender.path.name}, $executed,  $returnValue)"
}

abstract class Operation {
  def mid: String
}

case class Init(replicas: Set[ReplicaNode], myNode: ReplicaNode)

case class Get(key: String, mid: String) extends Operation

case class Put(key: String, value: String, mid: String) extends Operation

case class AddReplica(replica: ReplicaNode, mid: String) extends Operation

case class RemoveReplica(replica: ReplicaNode, mid: String) extends Operation

case class History(history: Map[Long, Event], index: Long)

case class Reply(event: Event)

case class GetReply(value: String, mid: String)


