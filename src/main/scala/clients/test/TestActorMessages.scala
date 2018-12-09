package clients.test

import akka.actor.ActorRef
import replicas.statemachinereplication.Event
import utils.ReplicaNode

import scala.collection.immutable.Queue

case class StartTest(replicas: Set[ReplicaNode], clientActor: ActorRef, testDuration: Long)

object Validate

object State

case class StateDelivery(history: Map[Long, Event], store: Map[String, String],
                         toBeProposed: Queue[Event], replicas: Set[ReplicaNode])

case class TestGet(key: String, mid: String)

case class TestPut(key: String, value: String, mid: String)

case class TestAddReplica(node: ReplicaNode, mid: String)

case class TestRemoveReplica(node: ReplicaNode, mid: String)