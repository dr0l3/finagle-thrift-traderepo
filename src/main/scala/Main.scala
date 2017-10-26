import com.twitter.server.TwitterServer

object Main extends TwitterServer {
  import com.twitter.finagle.Thrift
  import com.dr0l3.trades.thriftscala._
  import com.twitter.finagle.examples.names.thriftscala._
  import com.redis._

  val port = 9097
  val address = s"localhost:$port"

  val service = SimpleTradeRepoService.createWithClient(new RedisClient("localhost", 32768))

  val server = Thrift.serveIface(address, service)

  onExit {
    server.close()
  }


  val client = Thrift.newIface[TradeRepoService.FutureIface](address)

  val insert = client.create("USDEUR", 1.00, "Rune", "HisBank")

  val again = client.findById(insert.get.id)

  val res = again.get.head.equals(insert.get)

  println(res)
  println("hello")


}
