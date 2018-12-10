package utils

object Utils {
  def majority[A](nElems: Int, set: Set[A]) =
    nElems > set.size / 2

  def getConf(ip: String, port: String) = {
    s"""
       |akka {
       |  event-handlers = ["akka.event.slf4j.Slf4jEventHandler"]
       |   loglevel = "DEBUG"
       |   actor {
       |     provider = "remote"
       |     warn-about-java-serializer-usage = false
       |   }
       |   remote {
       |     enabled-transports = ["akka.remote.netty.tcp"]
       |     netty.tcp {
       |       hostname = "$ip"
       |       port = $port
       |     }
       |   }
       |  }
    """.stripMargin
  }
}


