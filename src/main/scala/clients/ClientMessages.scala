package clients

import replicas.statemachinereplication.{Event, Operation}
import utils.ReplicaNode


case class InitClient(replicas: Set[ReplicaNode], smr: Int)

case class Get(key: String)

case class Put(key: String, value: String)

case class AddReplica(node: ReplicaNode)

case class RemoveReplica(node: ReplicaNode)

case class ResendOp(put: Operation)

case class ReplyDelivery(event: Event)
