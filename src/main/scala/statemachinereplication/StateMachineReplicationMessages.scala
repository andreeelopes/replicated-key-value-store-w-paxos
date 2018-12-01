package statemachinereplication

import akka.actor.ActorRef

case class updateReplicas(replicas: Set[ActorRef])

