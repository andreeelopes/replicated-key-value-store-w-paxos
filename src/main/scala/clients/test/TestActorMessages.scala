package clients.test

import akka.actor.ActorRef
import replicas.statemachinereplication.Event
import utils.ReplicaNode

import scala.collection.immutable.Queue

case class StartTest(clientActor: ActorRef, testDuration: Long, testType: Int)

case class Validate()

case class State()

case class StateDelivery(history: Map[Long, List[Event]], store: Map[String, String],
                         toBeProposed: Queue[Event], replicas: Set[ReplicaNode])

case class TestWeakGet(key: String, mid: String)

case class TestStrongGet(key: String, mid: String)

case class TestPut(key: String, value: String, mid: String)

case class TestAddReplica(node: ReplicaNode, mid: String)

case class TestRemoveReplica(node: ReplicaNode, mid: String)