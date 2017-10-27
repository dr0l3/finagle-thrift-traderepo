package com.dr0l3.trades.thriftscala

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}

import com.redis.RedisClient
import com.twitter.util.{Future, Try}
import com.redis.serialization.{Format, Parse}
import thrift._

class SimpleTradeRepoService(db: DB) extends TradeService[Future] {
  var trades = List.empty[FXTrade]

  implicit val formatter: PartialFunction[Any, Any] = {
    case a => serialise(a)
  }
  implicit val parser = Parse.apply(deserialise)
  implicit val format = Format.apply(formatter)

  def serialise(value: Any): Array[Byte] = {
    val stream: ByteArrayOutputStream = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(stream)
    oos.writeObject(value)
    oos.close()
    stream.toByteArray
  }

  def deserialise(bytes: Array[Byte]): FXTrade = {
    val ois = new ObjectInputStream(new ByteArrayInputStream(bytes))
    val value = ois.readObject
    ois.close()
    value.asInstanceOf[FXTrade]
  }


  override def create(ccypair: String, rate: Double, buyer: TradingEntity, seller: TradingEntity) = Future {
    val id = java.util.UUID.randomUUID().toString
    val newTrade = FXTrade(id, ccypair, rate, buyer, seller)
    val sellerCount = db.findById(newTrade.seller.id).map(_.counter)
    val buyerCount = db.findById(newTrade.buyer.id).map(_.counter)
    if (buyerCount.getOrElse(0) >= newTrade.buyer.counter) {
      throw AppException("Buyer count lower than last seen")
    } else if (sellerCount.getOrElse(0) >= newTrade.seller.counter) {
      throw AppException("Seller count lower than last seen")
    } else {
      db.increment(newTrade.buyer)
      db.increment(newTrade.seller)
      db.insert(newTrade).getOrElse(throw AppException("Error while inserting trade"))
    }
  }

  override def update(updated: FXTrade) = Future {
    db.update(updated)
  }

  override def deleteById(id: String) = Future {
   db.deleteById(id).getOrElse(throw NotFoundException(s"trade with id $id not found"))
  }

  override def delete(todelete: FXTrade) = {
    deleteById(todelete.id)
  }

  override def findById(id: String) = Future {
    db.get(id).getOrElse(throw NotFoundException(s"trade with id $id not found"))
  }

  //  override def findByCcyPair(ccypair: String) = {
  //    Future(trades.filter(t => t.ccypair == ccypair))
  //  }
  //
  //  override def findByBuyer(buyer: String) = {
  //    Future(trades.filter(t => t.buyer == buyer))
  //  }
  //
  //  override def findBySeller(seller: String) = {
  //    Future(trades.filter(t => t.seller == seller))
  //  }
  override def findByName(name: String) = Future {
    db.findById(name).getOrElse(throw NotFoundException(s"Trader with id $name not found"))
  }

  override def createTrader(name: String): Future[TradingEntity] = Future {
    if(db.findById(name).isDefined){
      throw AppException("Name already taken")
    }
    db.createTradingEntity(TradingEntity(name,1)).getOrElse(throw AppException("Unable to create trader"))
  }
}

object SimpleTradeRepoService {
  def create(): TradeService[Future] = {
//    val client = new RedisClient("localhost", 32768)
    new SimpleTradeRepoService(new InMemoryDB)
  }

  def createWithClient(redisClient: RedisClient): TradeService[Future] = {
    new SimpleTradeRepoService(new RedisDB(redisClient))
  }
}


