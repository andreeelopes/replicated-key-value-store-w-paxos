package replicas.multidimensionalpaxos

import replicas.statemachinereplication.Event
import utils.ReplicaNode

case class InitPaxos(myNode: ReplicaNode)

case class Propose(value: List[Event], instance: Long)

case class Prepare(sn: Int, instance: Long)

case class PrepareOk(sna: Int, va: List[Event], instance: Long, snSent: Int)

case class Accept(sna: Int, va: List[Event], instance: Long)

case class AcceptOk(sna: Int, instance: Long, snSent: Int)

case class LockedValue(value: List[Event], instance: Long)

case class DecisionDelivery(decision: List[Event], instance: Long)

case class PrepareTimer(instance: Long)

case class AcceptTimer(instance: Long)
