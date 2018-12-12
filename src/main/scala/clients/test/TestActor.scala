package clients.test

import akka.actor.{Actor, ActorLogging, ActorRef}
import clients.{Put, ReplyDelivery}
import replicas.statemachinereplication.{Event, GetReply, UpdateReplicas}
import utils.ReplicaNode

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit



class TestActor() extends Actor with ActorLogging {

  case class OperationMetrics(time: Long, delivered: Boolean = false)

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



  override def receive = {

    case s: StartTest =>
      receiveStartTest(s)

    case Validate() =>
      replicas.foreach(r => r.smrActor ! State())

    case state: StateDelivery =>
      states ::= state

      if (states.size == replicas.size) {
        validateReplicasState()
        calculateMetrics()
      }

    case ReplyDelivery(event) =>
      receiveReplyDelivery(event)

    case GetReply(_, _mid_) =>
      receiveReplyDelivery(Event(null, _mid_, null, null))


    case a :UpdateReplicas =>
      replicas = a.replicas
      //log.info(s"replicas: $replicas")


  }



  def calculateMetrics(): Unit = {

    val timesOfOpsExecuted = opsTimes.values.filter(metrics => metrics.delivered).map(m => m.time)
    throughput = timesOfOpsExecuted.size / (testDuration / 1000.0)
    latency = timesOfOpsExecuted.sum / timesOfOpsExecuted.size.toDouble

    log.info(s"\n>>>Operations Times:\n${timesOfOpsExecuted.take(50)}\n")
    log.info(s"Executed $mid operations (mid) | ${opsTimes.size}")
    log.info(s"latency: $latency ms, throughput: $throughput ops/s\n\t\t\t\t\t\t\ttimesOfOpsExecuted:" +
      s" ${timesOfOpsExecuted.size}, testDuration: $testDuration, timesOfOpsExecuted.sum: ${timesOfOpsExecuted.sum}\n\n")
  }


  private def validateReplicasState(): Unit = {
    var valid: Boolean = true
    log.error(s">>> Starting Validation...") //\n\nstates=$states\n\n")
    if (states.count { state => state.history.equals(states.head.history) } != states.size) {
      log.error("Different histories!")
      valid = false
    }

// TODO   if(states.count{state => state.history.filter(p=>p._2.executed).equals(states.head.history.filter(p=>p._2.executed))}  != states.size){
//      log.error("Different executed ops!")
//      valid = false
//    }

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
      states.foreach(s => log.error(s"\n\nqueue = ${s.toBeProposed.size}"))

      valid = false

    }
    if (valid)
      log.info(">>> Validation completed with Success!")

  }


  def receiveReplyDelivery(event: Event): Unit = {
    if (System.currentTimeMillis() - testStart <= testDuration) {
      //log.info(s"\nRecebi ${event.mid}\n")
      opsTimes += (event.mid -> OperationMetrics(System.currentTimeMillis() - opsTimes(event.mid).time, delivered = true))
      executeTestOp()
    }
  }

  def receiveStartTest(s: StartTest): Unit = {
    clientActor = s.clientActor
    testDuration = s.testDuration
    testStart = System.currentTimeMillis()
    executeTestOp()
  }

  def executeTestOp(): Unit = {
    if (System.currentTimeMillis() - testStart <= testDuration) {

      clientActor ! TestPut(KeyA, mid.toString, mid.toString)
      //clientActor ! TestGet(KeyA, mid.toString)
      opsTimes += (mid.toString -> OperationMetrics(System.currentTimeMillis()))

      mid += 1
    }
  }

//  def receiveExecuteTest1(): Unit = {
//    if (System.currentTimeMillis() - testStart <= testDuration) {
//
//      clientActor ! TestGet(KeyA, mid.toString)
//      opsTimes += (mid.toString -> OperationMetrics(System.currentTimeMillis()))
//
//      mid += 1
//    }
//  }

}
