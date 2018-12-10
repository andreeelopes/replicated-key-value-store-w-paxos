import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import rendezvous.RendezvousActor
import replicas.multidimensionalpaxos.InitPaxos
import replicas.multidimensionalpaxos.acceptors.AcceptorActor
import replicas.multidimensionalpaxos.learners.LearnerActor
import replicas.multidimensionalpaxos.proposers.ProposerActor
import replicas.statemachinereplication.{InitSmr, StateMachineReplicationActor}
import utils.{ReplicaNode, Utils}

object ReplicaMain extends App {
  override def main(args: Array[String]) = {
    val ip = args(0)
    val port = args(1)
    val configuration = ConfigFactory.parseString(Utils.getConf(ip,port))


    val rendezvousIP = "127.0.0.1"
    val rendezvousPort = 69

    val replicaSystem = ActorSystem("RemoteService", config = configuration)


    val paxosProposer = replicaSystem.actorOf(Props[ProposerActor], "paxosProposer")
    val paxosAcceptor = replicaSystem.actorOf(Props[AcceptorActor], "paxosAcceptor")
    val paxosLearner = replicaSystem.actorOf(Props[LearnerActor], "paxosLearner")
    val smrActor = replicaSystem.actorOf(Props(new StateMachineReplicationActor(rendezvousIP, rendezvousPort)), "smrActor")

    val node = ReplicaNode(s"node:$port", port, smrActor, paxosAcceptor, paxosLearner, paxosProposer)


    paxosProposer ! InitPaxos(node)
    paxosAcceptor ! InitPaxos(node)
    paxosLearner ! InitPaxos(node)
    smrActor ! InitSmr(node)
  }
}