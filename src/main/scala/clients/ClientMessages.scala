package clients

import akka.actor.ActorRef
import replicas.statemachinereplication.{Event, Operation, WeakGetRequest}
import utils.ReplicaNode


case class InitClient(smr: Int, appActor: ActorRef)

case class WeakGet(key: String)

case class StrongGet(key: String)

case class Put(key: String, value: String)

case class AddReplica(node: ReplicaNode)

case class RemoveReplica(node: ReplicaNode)

case class ResendOp(put: Operation)

case class ReplyDelivery(event: Event)

case class ResendWeakOp(weakOp: WeakGetRequest)