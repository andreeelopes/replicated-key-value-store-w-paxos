package multidimensionalpaxos

import utils.Node

case class Init(membership: Set[Node], myNode: Node)

case class Propose(value: String, instance: Long)

case class Prepare(sn: Int, instance: Long)

case class PrepareOk(sna: Int, va: String, instance: Long)

case class Accept(sna: Int, va: String, instance: Long)

case class AcceptOk(sna: Int, instance: Long)

case class LockedValue(value: String, instance: Long)

case class DecisionDelivery(decision: String, instance: Long)

case class PrepareTimer(instance: Long)

case class AcceptTimer(instance: Long)
