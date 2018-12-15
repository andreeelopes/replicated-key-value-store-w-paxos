package replicas.paxos

import utils.ReplicaNode

case class Init(membership: Set[ReplicaNode], myNode: ReplicaNode)

case class Propose(value: String)

case class Prepare(sn: Int)

case class PrepareOk(sna: Int, va: String)

object PrepareNotOk

case class Accept(sna: Int, va: String)

case class AcceptOk(sna: Int)

case class LockedValue(value: String)

case class DecisionDelivery(decision: String)

object PrepareTimer

object AcceptTimer