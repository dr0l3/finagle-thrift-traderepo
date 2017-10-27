import com.twitter.server.TwitterServer
import com.twitter.util.{Await, Duration, Future}
import thrift.{TradeService, TradingEntity}

object Main extends TwitterServer {
  import com.twitter.finagle.Thrift
  import com.dr0l3.trades.thriftscala._
  import com.redis._

  val port = 9108
  val address = s"localhost:$port"

  val service = SimpleTradeRepoService.createWithClient(new RedisClient("localhost", 32768))

  val server = Thrift.serveIface(address, service)

  onExit {
    server.close()
  }


  val client = Thrift.newIface[TradeService.FutureIface](address)

  val buyer = client.findByName("Rune").get()
  println(buyer)
  val seller = client.findByName("Bank").get()
  println(seller)

  client.create("USDEUR", 1.00, buyer.copy(counter = buyer.counter+1), seller.copy(counter = seller.counter +1))
    .flatMap(inserted => client.findById(inserted.id))
    .onFailure(e => println(e.toString))
    .onSuccess(found => println(s"Succesfully inserted and retrieved ${found.toString}"))



  val b = client.findById("invalidId")
    .onFailure(e => println(e.toString))
    .onSuccess(v => println(v))


  Thread.sleep(5000)
}
