package com.dr0l3.trades.thriftscala

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}

import com.redis.RedisClient
import com.twitter.finagle.examples.names.thriftscala._
import com.twitter.util.{Future, Try}
import com.redis.serialization.{Format, Parse}

class SimpleTradeRepoService(redisClient: RedisClient) extends TradeRepoService[Future] {
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

  /** Notes: Redis is blocking
    *
    */


  override def create(ccypair: String, rate: Double, buyer: String, seller: String) = Future {
    val id = java.util.UUID.randomUUID().toString
    val newTrade = FXTrade(id, ccypair, rate, buyer, seller)
    val res = redisClient.set(newTrade.id, newTrade)
    if (res) newTrade else throw MyCustomException(s"Unable to write value ${newTrade.toString}")
  }

  override def update(updated: FXTrade) = Future {
    val res = redisClient.set(updated.id, updated)
    if (res) updated else throw MyCustomException(s"Unable to write value ${updated.toString}")
  }

  override def deleteById(id: String) = Future {
    redisClient.del(id).map(count => {
      val current = redisClient.get(id).get
      if (count < 1) current else throw MyCustomException(s"No value found with $id")
    }).getOrElse(throw RedisException("Error while connecting to redis"))
  }

  override def delete(todelete: FXTrade) = {
    deleteById(todelete.id)
  }

  override def findById(id: String) = Future {
    redisClient.get[FXTrade](id)
      .getOrElse(throw MyCustomException(s"No value found with $id"))
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
}

object SimpleTradeRepoService {
  def create(): TradeRepoService[Future] = {
    val client = new RedisClient("localhost", 32768)
    new SimpleTradeRepoService(client)
  }

  def createWithClient(redisClient: RedisClient): TradeRepoService[Future] = {
    new SimpleTradeRepoService(redisClient)
  }
}


