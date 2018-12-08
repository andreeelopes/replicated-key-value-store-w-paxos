import akka.actor.{ActorSystem, PoisonPill, Props}
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
    val d = ActorSystem("nodeDsystem")
    val e = ActorSystem("nodeESystem")
    val f = ActorSystem("nodeFSytstem")

    val aPaxosProposer = a.actorOf(Props[ProposerActor], "aPaxosProposer")
    val bPaxosProposer = b.actorOf(Props[ProposerActor], "bPaxosProposer")
    val cPaxosProposer = c.actorOf(Props[ProposerActor], "cPaxosProposer")
    val dPaxosProposer = d.actorOf(Props[ProposerActor], "dPaxosProposer")
    val ePaxosProposer = e.actorOf(Props[ProposerActor], "ePaxosProposer")
    val fPaxosProposer = f.actorOf(Props[ProposerActor], "fPaxosProposer")

    val aPaxosAcceptor = a.actorOf(Props[AcceptorActor], "aPaxosAcceptor")
    val bPaxosAcceptor = b.actorOf(Props[AcceptorActor], "bPaxosAcceptor")
    val cPaxosAcceptor = c.actorOf(Props[AcceptorActor], "cPaxosAcceptor")
    val dPaxosAcceptor = d.actorOf(Props[AcceptorActor], "dPaxosAcceptor")
    val ePaxosAcceptor = e.actorOf(Props[AcceptorActor], "ePaxosAcceptor")
    val fPaxosAcceptor = f.actorOf(Props[AcceptorActor], "fPaxosAcceptor")

    val aPaxosLearner = a.actorOf(Props[LearnerActor], "aPaxosLearner")
    val bPaxosLearner = b.actorOf(Props[LearnerActor], "bPaxosLearner")
    val cPaxosLearner = c.actorOf(Props[LearnerActor], "cPaxosLearner")
    val dPaxosLearner = d.actorOf(Props[LearnerActor], "dPaxosLearner")
    val ePaxosLearner = e.actorOf(Props[LearnerActor], "ePaxosLearner")
    val fPaxosLearner = f.actorOf(Props[LearnerActor], "fPaxosLearner")

    val aNode = Node("aNode", "1", null, null, aPaxosAcceptor, aPaxosLearner, aPaxosProposer)
    val bNode = Node("bNode", "2", null, null, bPaxosAcceptor, bPaxosLearner, bPaxosProposer)
    val cNode = Node("cNode", "3", null, null, cPaxosAcceptor, cPaxosLearner, cPaxosProposer)
    val dNode = Node("dNode", "4", null, null, dPaxosAcceptor, dPaxosLearner, dPaxosProposer)
    val eNode = Node("eNode", "5", null, null, ePaxosAcceptor, ePaxosLearner, ePaxosProposer)
    val fNode = Node("fNode", "6", null, null, fPaxosAcceptor, fPaxosLearner, fPaxosProposer)

    val membership = Set(aNode, bNode, cNode, dNode, eNode, fNode)
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

    dPaxosProposer ! Init(membership, dNode)
    dPaxosAcceptor ! Init(membership, dNode)
    dPaxosLearner ! Init(membership, dNode)

    ePaxosProposer ! Init(membership, eNode)
    ePaxosAcceptor ! Init(membership, eNode)
    ePaxosLearner ! Init(membership, eNode)

    fPaxosProposer ! Init(membership, fNode)
    fPaxosAcceptor ! Init(membership, fNode)
    fPaxosLearner ! Init(membership, fNode)


    //Test
    aNode.proposerActor ! Propose("a")

  }
}
