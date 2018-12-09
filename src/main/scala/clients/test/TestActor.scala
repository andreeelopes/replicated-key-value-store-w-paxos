package clients.test

import akka.actor.{Actor, ActorLogging, ActorRef}
import clients.{Put, ReplyDelivery}
import replicas.statemachinereplication.{Event, GetReply}
import utils.ReplicaNode

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit


class TestActor extends Actor with ActorLogging {

  case class OperationMetrics(time: Long, delivered: Boolean = false)

  object ExecuteTest1

  val PutOperations = 100
  val KeyA = "keyA"

  var replicas: Set[ReplicaNode] = _
  var states = List[StateDelivery]()
  var clientActor: ActorRef = _
  var throughput: Double = 0.0
  var latency: Double = 0.0
  var opsTimes = Map[String, OperationMetrics]()
  var testDuration: Long = _
  var testStart: Long = _
  var mid = 0

  def receiveReplyDelivery(event: Event): Unit = {
    if (System.currentTimeMillis() - testStart <= testDuration) {
      //          log.info(s"\nola\n")
      opsTimes += (event.mid -> OperationMetrics(System.nanoTime() - opsTimes(event.mid).time, delivered = true))
    }
}

  def calculateMetrics(): Unit = {

    var timesOfOpsExecuted = opsTimes.values.filter(metrics => metrics.delivered).map(m => m.time)
    throughput = timesOfOpsExecuted.size / (testDuration / 1000.0)
    latency = timesOfOpsExecuted.sum / timesOfOpsExecuted.size

    log.info(s"latency: $latency ms, throughput: $throughput ops/s\ntimesOfOpsExecuted: ${timesOfOpsExecuted.size}, testDuration: $testDuration, timesOfOpsExecuted.sum: ${timesOfOpsExecuted.sum}")
  }

  override def receive = {

    case s: StartTest =>
      receiveStartTest(s)

    case Validate =>
      replicas.foreach(r => r.smrActor ! State)

    case state: StateDelivery =>
      states ::= state

      if (states.size == replicas.size) {
        validateReplicasState()
        calculateMetrics()
      }

    case ReplyDelivery(event) =>
      receiveReplyDelivery(event)

    case GetReply(_, mid) =>
      receiveReplyDelivery(Event(null, mid, null, null))

    case ExecuteTest1 =>
      receiveExecuteTest1()


  }

  private def validateReplicasState(): Unit = {
    var valid: Boolean = true
    log.info(s">>> Starting Validation...") //\n\nstates=$states\n\n")
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


  def receiveStartTest(s: StartTest): Unit = {
    replicas = s.replicas
    clientActor = s.clientActor
    testDuration = s.testDuration
    testStart = System.currentTimeMillis()
    context.system.scheduler.schedule(Duration(0, TimeUnit.SECONDS), Duration(0, TimeUnit.NANOSECONDS), self, ExecuteTest1)

  }

  def receiveExecuteTest1(): Unit = {
    if (System.currentTimeMillis() - testStart <= testDuration) {

      clientActor ! TestGet(KeyA, mid.toString)
      opsTimes += (mid.toString -> OperationMetrics(System.nanoTime()))

      mid += 1
    }
  }

}
