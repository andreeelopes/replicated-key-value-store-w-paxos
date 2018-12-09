package clients.test

import akka.actor.{Actor, ActorLogging}
import utils.ReplicaNode

class TestActor extends Actor with ActorLogging {

  var replicas: Set[ReplicaNode] = _
  var states = List[StateDelivery]()

  var througput: Double = 0.0
  var latency: Double = 0.0


  override def receive = {

    case StartTest(_replicas_, clientActor) =>
      replicas = _replicas_

      receiveStartTest()

    case Validate =>
      replicas.foreach(r => r.smrActor ! State)

    case state: StateDelivery =>
      states ::= state

      if (states.size == replicas.size) {
        validateReplicasState()
      }

  }

  private def validateReplicasState() : Unit = {
    var valid: Boolean = true
    log.info(s">>> Starting Validation...\n\nstates=$states\n\n")
    if (states.count { state => state.history.equals(states.head.history) } != states.size) {
      log.error("Different histories!")
      valid = false
    }
    if (states.count { state => state.replicas.equals(states.head.replicas) } != states.size) {
      log.error("Different replicas!")
      valid = false

    }
    if (states.count { state => state.store.equals(states.head.store) } != states.size) {
      log.error("Different store!")
      valid = false

    }
    if (states.count { state => state.toBeProposed.equals(states.head.toBeProposed) } != states.size) {
      log.error("Different toBeProposed queue!")
      valid = false

    }
    if (valid)
      log.info(">>> Validation completed with Success!")

  }

  def receiveStartTest(): Unit = {

  }


}
