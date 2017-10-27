import com.twitter.util.Try
import org.scalatest.{FlatSpec, Matchers}
import thrift.{FXTrade, TradingEntity}

class Test extends FlatSpec with Matchers {
  import com.dr0l3.trades.thriftscala._


  val service = SimpleTradeRepoService.create()


  "An inserted trade" should "be fetchable" in {
    val buyer = service.createTrader("Rune").get
    val seller = service.createTrader("Bank").get

    val buyerIncremented = buyer.copy(counter = buyer.counter +1)
    val sellerIncremented = seller.copy(counter = seller.counter +1)

    val res = service.create("USDEUR",1.00, buyerIncremented, sellerIncremented)
      .flatMap(t => service.findById(t.id))
      .get()

    res.buyer.id should be ("Rune")
  }

  "An nonexistent trade" should "not be fetchable" in {
    val res = Try(service.findById("Hello").get())

    res.isThrow should be (true)
  }

  "Update with nonexistent trade" should "insert the trade" in {
    val buyer = service.createTrader("Trader1").get
    val seller = service.createTrader("Trader2").get

    val buyerIncremented = buyer.copy(counter = buyer.counter +1)
    val sellerIncremented = seller.copy(counter = seller.counter +1)

    val trade = FXTrade("myId","USDEUR",1.00,buyerIncremented, sellerIncremented)
    service.update(trade)
    val res = service.findById("myId").get()

    res.id should be ("myId")
  }

  "Update with existing trade" should "update the trade" in {
    val buyer = TradingEntity("Rune",2)
    val seller = TradingEntity("Blank",2)

    val buyerIncremented = buyer.copy(counter = buyer.counter +1)
    val sellerIncremented = seller.copy(counter = seller.counter +1)

    val created = service.create("USDEUR",1.00,buyerIncremented, sellerIncremented).get
    val updated = created.copy(seller = TradingEntity("Bank",seller.counter))
    val res = service.update(updated = updated).get

    res.seller.id should be ("Bank")
  }

  "Deleting a trade" should "make it not fetchable" in {
    val buyer = TradingEntity("Rune",4)
    val seller = TradingEntity("Bank",4)
    val created = service.create("USDEUR",1.00,buyer, seller).get
    val fetched = service.findById(created.id).get
    service.deleteById(fetched.id)
    val res = Try(service.findById(fetched.id).get())

    res.isThrow should be (true)
  }

  "Trading with too low id" should "fail" in {
    val buyer = service.createTrader("Trader10").get()
    val seller = service.createTrader("Trader11").get()
    val res = Try(service.create("USDEUR",1.00,buyer,seller).get())

    res.isThrow should be (true)
  }

  "Trading" should "bump ids" in {
    val buyer = service.createTrader("FreshBuyer").get()
    val seller = service.createTrader("FreshSeller").get()
    val buyerIncremented = buyer.copy(counter = buyer.counter +1)
    val sellerIncremented = seller.copy(counter = seller.counter +1)
    val res = Try(service.create("USDEUR",1.00,buyerIncremented,sellerIncremented).get())

    val buyerAfter = service.findByName(buyer.id).get()
    val sellerAfter = service.findByName(seller.id).get()
    buyerAfter.counter should be (buyer.counter+1)
    sellerAfter.counter should be (seller.counter+1)
  }

}
