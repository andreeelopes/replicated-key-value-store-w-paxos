package replicas.multidimensionalpaxos

import replicas.statemachinereplication.{Event}
import utils.ReplicaNode

case class Init(membership: Set[ReplicaNode], myNode: ReplicaNode)

case class Propose(value: Event, instance: Long)

case class Prepare(sn: Int, instance: Long)

case class PrepareOk(sna: Int, va: Event, instance: Long)

case class Accept(sna: Int, va: Event, instance: Long)

case class AcceptOk(sna: Int, instance: Long)

case class LockedValue(value: Event, instance: Long)

case class DecisionDelivery(decision: Event, instance: Long)

case class PrepareTimer(instance: Long)

case class AcceptTimer(instance: Long)
