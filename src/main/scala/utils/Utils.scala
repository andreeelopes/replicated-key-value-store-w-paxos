package utils

object Utils {
  def majority[A](nElems: Int, set: Set[A]) =
    nElems > set.size / 2

  def getConf(ip: String, port: String) = {
    s"""
       |akka {
       |  event-handlers = ["akka.event.slf4j.Slf4jEventHandler"]
       |
       |   actor {
       |     provider = "remote"
       |     warn-about-java-serializer-usage = false
       |   }
       |   remote {
       |   maximum-payload-bytes = 30000000 bytes
       |     enabled-transports = ["akka.remote.netty.tcp"]
       |     netty.tcp {
       |       hostname = "$ip"
       |       port = $port
       |      message-frame-size =  30000000b
       |      send-buffer-size =  30000000b
       |      receive-buffer-size =  30000000b
       |      maximum-frame-size = 30000000b
       |     }
       |   }
       |  }
    """.stripMargin
  }
}
