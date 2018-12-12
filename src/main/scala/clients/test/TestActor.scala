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
  val PutOpType = 0
  val GetOpType = 1

  var replicas: Set[ReplicaNode] = _
  var states = List[StateDelivery]()
  var clientActor: ActorRef = _
  var throughput: Double = 0.0
  var latency: Double = 0.0
  var opsTimes = Map[String, OperationMetrics]()
  var testDuration: Long = _
  var testStart: Long = _
  var mid = 0
  var testType = 0 //0=PUT 1=GET


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


    case a: UpdateReplicas =>
      replicas = a.replicas
    //println(s"replicas: $replicas")


  }


  def calculateMetrics(): Unit = {

    val timesOfOpsExecuted = opsTimes.values.filter(metrics => metrics.delivered).map(m => m.time)
    throughput = timesOfOpsExecuted.size / (testDuration / 1000.0)
    latency = timesOfOpsExecuted.sum / timesOfOpsExecuted.size.toDouble

    println(s"\n>>>Operations Times:\n${timesOfOpsExecuted.take(50)}\n")
    println(s"Executed $mid operations (mid) | ${opsTimes.size}")
    println(s"latency: $latency ms, throughput: $throughput ops/s\n\t\t\t\t\t\t\ttimesOfOpsExecuted:" +
      s" ${timesOfOpsExecuted.size}, testDuration: $testDuration, timesOfOpsExecuted.sum: ${timesOfOpsExecuted.sum}\n\n")
  }


  private def validateReplicasState(): Unit = {
    var valid: Boolean = true
    log.error(s">>> Starting Validation...") //\n\nstates=$states\n\n")

    println("Histories size:")
    states.foreach(s => print(s.history.size + "\t"))

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
      states.foreach(s => log.error(s"Queue = ${s.toBeProposed.size}"))

      valid = false

    }
    if (valid)
      log.error(">>> Validation completed with Success!")

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
    testType = s.testType
    testStart = System.currentTimeMillis()
    executeTestOp()
  }

  def executeTestOp(): Unit = {
    if (System.currentTimeMillis() - testStart <= testDuration) {

      if (testType == PutOpType) {
        executePutTest()
      }
      else if (testType == GetOpType) {
        executeGetTest()
      }

      opsTimes += (mid.toString -> OperationMetrics(System.currentTimeMillis()))

      mid += 1
    }
  }

  def executeGetTest() = {
    clientActor ! TestWeakGet(KeyA, mid.toString)
  }

  def executePutTest() = {
    clientActor ! TestPut(KeyA, mid.toString, mid.toString)
  }

}
