import com.twitter.server.TwitterServer
import com.twitter.util.{Await, Duration, Future}

object Main extends TwitterServer {
  import com.twitter.finagle.Thrift
  import com.dr0l3.trades.thriftscala._
  import com.twitter.finagle.examples.names.thriftscala._
  import com.redis._

  val port = 9099
  val address = s"localhost:$port"

  val service = SimpleTradeRepoService.createWithClient(new RedisClient("localhost", 32768))

  val server = Thrift.serveIface(address, service)

  onExit {
    server.close()
  }


  val client = Thrift.newIface[TradeRepoService.FutureIface](address)

  val a = client.create("USDEUR", 1.00, "Rune", "HisBank")
    .flatMap(inserted => client.findById(inserted.id))
    .onSuccess(found => println(s"Succesfully inserted and retrieved ${found.toString}"))
    .onFailure(e => println(e.toString))


  val b = client.findById("invalidId")
    .onFailure(e => println(e.toString))
    .onSuccess(v => println(v))


  Thread.sleep(1000)
}
