package com.dr0l3.trades.thriftscala

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}

import com.twitter.finagle.examples.names.thriftscala.{FXTrade, TradeRepoService}
import com.twitter.util.{Future, Try}
import com.redis._

class SimpleTradeRepoService() extends TradeRepoService[Future] {
  var trades = List.empty[FXTrade]

  def serialise(value: Any): Array[Byte] = {
    val stream: ByteArrayOutputStream = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(stream)
    oos.writeObject(value)
    oos.close()
    stream.toByteArray
  }

  def deserialise(bytes: Array[Byte]): Any = {
    val ois = new ObjectInputStream(new ByteArrayInputStream(bytes))
    val value = ois.readObject
    ois.close()
    value
  }

  override def create(ccypair: String, rate: Double, buyer: String, seller: String) = {
    val id = java.util.UUID.randomUUID().toString
    val newTrade =FXTrade(id,ccypair,rate,buyer,seller)
//    redisClient.set(newTrade.id,newTrade)
    trades = newTrade :: trades
    Future(newTrade)
  }

  override def update(updated: FXTrade) = {
    val id = updated.id
    trades = updated :: trades.filterNot(t => t.id == id)
    Future(List(updated))
  }

  override def deleteById(id: String) = {
    val todelete = trades.find(_.id == id)
    trades = trades.filterNot(t => t.id == id)
    Future(todelete.toList)
  }

  override def delete(todelete: FXTrade) = {
    deleteById(todelete.id)
  }

  override def findById(id: String) = {
    Future(trades.find(_.id == id).toList)
  }

  override def findByCcyPair(ccypair: String) = {
    Future(trades.filter(t => t.ccypair == ccypair))
  }

  override def findByBuyer(buyer: String) = {
    Future(trades.filter(t => t.buyer == buyer))
  }

  override def findBySeller(seller: String) = {
    Future(trades.filter(t => t.seller == seller))
  }

}

object SimpleTradeRepoService {
  def create(): TradeRepoService[Future] = {
    val client = new RedisClient("localhost", 6379)
    new SimpleTradeRepoService()
  }
}


