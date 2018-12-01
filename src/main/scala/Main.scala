import akka.actor.{ActorSystem, Props}
import paxos.{Init, Propose}
import paxos.acceptors.AcceptorActor
import paxos.learners.LearnerActor
import paxos.proposers.ProposerActor
import utils.Node

object Main extends App {

  override def main(args: Array[String]) = {

    val a = ActorSystem("nodeAsystem")
    val b = ActorSystem("nodeBSystem")
    val c = ActorSystem("nodeCSytstem")

    val aPaxosProposer = a.actorOf(Props[ProposerActor], "aPaxosProposer")
    val bPaxosProposer = b.actorOf(Props[ProposerActor], "bPaxosProposer")
    val cPaxosProposer = c.actorOf(Props[ProposerActor], "cPaxosProposer")

    val aPaxosAcceptor = a.actorOf(Props[AcceptorActor], "aPaxosAcceptor")
    val bPaxosAcceptor = a.actorOf(Props[AcceptorActor], "bPaxosAcceptor")
    val cPaxosAcceptor = a.actorOf(Props[AcceptorActor], "cPaxosAcceptor")

    val aPaxosLearner = a.actorOf(Props[LearnerActor], "aPaxosLearner")
    val bPaxosLearner = a.actorOf(Props[LearnerActor], "bPaxosLearner")
    val cPaxosLearner = a.actorOf(Props[LearnerActor], "cPaxosLearner")

    val aNode = Node("aNode", null, null, aPaxosAcceptor, aPaxosLearner, aPaxosProposer)
    val bNode = Node("bNode", null, null, bPaxosAcceptor, bPaxosLearner, bPaxosProposer)
    val cNode = Node("cNode", null, null, cPaxosAcceptor, cPaxosLearner, cPaxosProposer)

    val membership = Set(aNode, bNode, cNode)
    //Init
    aPaxosProposer ! Init(membership, aNode)
    aPaxosAcceptor ! Init(membership, aNode)
    aPaxosLearner ! Init(membership, aNode)

    bPaxosProposer ! Init(membership, bNode)
    bPaxosAcceptor ! Init(membership, bNode)
    bPaxosLearner ! Init(membership, bNode)

    cPaxosProposer ! Init(membership, cNode)
    cPaxosAcceptor ! Init(membership, cNode)
    cPaxosLearner ! Init(membership, cNode)

    println("enviando os proposes")

    aNode.proposerActor ! Propose("a")
    bNode.proposerActor ! Propose("b")
    cNode.proposerActor ! Propose("c")
  }
}
