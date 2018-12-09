package statemachinereplication

import akka.actor.ActorRef
import utils.Node

import scala.collection.immutable.Queue

case class UpdateReplicas(replicas: Set[Node])

case class Event(op: Operation, mid: String, sender: ActorRef, replica: Node,
                 var executed: Boolean = false, var returnValue: String = null) {
  override def toString = s"Event($op, $mid, ${sender.path.name}, $executed,  $returnValue)"
}

abstract class Operation {
  def mid: String
}

case class Init(replicas: Set[Node], myNode: Node)

case class Get(key: String, mid: String) extends Operation

case class Put(key: String, value: String, mid: String) extends Operation

case class AddReplica(replica: Node, mid: String) extends Operation

case class RemoveReplica(replica: Node, mid: String) extends Operation

case class History(history: Map[Long, Event], index: Long)

case class Reply(event: Event)

case class GetReply(value: String, mid: String)

object State

case class StateDelivery(history: Map[Long, Event], store: Map[String, String],
                         toBeProposed: Queue[Event], replicas: Set[Node])
