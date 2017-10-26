sbt console

```scala
import com.twitter.finagle.Thrift
import com.dr0l3.trades.thriftscala._
import com.twitter.finagle.examples.names.thriftscala._

val server = new SimpleTradeRepoService()

Thrift.serveIface("localhost:9090", server) 

```

and the client

```scala
import com.twitter.finagle.Thrift
import com.dr0l3.trades.thriftscala._
import com.twitter.finagle.examples.names.thriftscala._
val client = Thrift.newIface[TradeRepoService.FutureIface]("localhost:9090")

val insert = client.create("USDEUR", 1.00, "Rune", "HisBank")

val again = client.findById(insert.get.id)

again.get.head.equals(insert.get) 
```

bool should be true