package com.dr0l3.trades.thriftscala

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}

import com.redis.RedisClient
import com.redis.serialization.{Format, Parse}
import com.twitter.util.Try
import thrift.{AppException, FXTrade, RedisException, TradingEntity}

trait DB {
  def insert(trade: FXTrade): Option[FXTrade]

  def update(trade: FXTrade): FXTrade

  def delete(trade: FXTrade): Option[FXTrade]

  def get(id: String): Option[FXTrade]

  def deleteById(id: String): Option[FXTrade]

  def findById(id: String): Option[TradingEntity]

  def increment(tradingEntity: TradingEntity): Option[TradingEntity]

  def createTradingEntity(trader: TradingEntity): Option[TradingEntity]
}

class RedisDB(redisClient: RedisClient) extends DB {
  override def insert(trade: FXTrade) = {
    val res = redisClient.set(trade.id, trade)
    if (res) Some(trade) else throw RedisException(s"Unable to write value ${trade.toString}")
  }

  override def update(trade: FXTrade) = {
    val res = redisClient.set(trade.id, trade)
    if (res) trade else throw RedisException(s"Unable to write value ${trade.toString}")
  }

  override def delete(trade: FXTrade) = {
    deleteById(trade.id)
  }

  override def get(id: String) = {
    redisClient.get[FXTrade](id)
  }

  override def deleteById(id: String) = {
    val current = get(id)
    current.foreach(t => redisClient.del(t.id))
    current
  }

  implicit val formatter: PartialFunction[Any, Any] = {
    case a => serialise(a)
  }
  implicit val parser = Parse.apply[FXTrade](deserialise)
  implicit val format = Format.apply(formatter)
  implicit val traderParser = Parse.apply[TradingEntity](deserialiseTrader)

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

  def deserialiseTrader(bytes: Array[Byte]): TradingEntity = {
    val ois = new ObjectInputStream(new ByteArrayInputStream(bytes))
    val value = ois.readObject
    ois.close()
    value.asInstanceOf[TradingEntity]
  }

  override def findById(id: String) = {
    println(id)
    val res = Try(redisClient.get[TradingEntity](id))
    println(res)
    res.get()
  }

  override def increment(tradingEntity: TradingEntity) = {
    val current = findById(tradingEntity.id)
    if (current.isEmpty) {
      throw AppException("Incrementing nonexistendt trader")
    }
    current.flatMap(t => {
      val incremented = t.copy(counter = t.counter + 1)
      val success = redisClient.set(t.id, incremented)

      if (success) {
        Some(incremented)
      } else {
        None
      }
    })
  }

  override def createTradingEntity(trader: TradingEntity): Option[TradingEntity] = {
    val success = redisClient.set(trader.id, trader)
    if(success){
      Some(trader)
    } else {
      None
    }
  }
}

class InMemoryDB extends DB {
  var trades = List.empty[FXTrade]
  var entities = List.empty[TradingEntity]

  override def insert(trade: FXTrade) = {
    trades = trade :: trades
    Some(trade)
  }

  override def update(trade: FXTrade) = {
    trades = trade :: trades.filterNot(_.id == trade.id)
    trade
  }

  override def delete(trade: FXTrade) = {
    deleteById(trade.id)
  }

  override def get(id: String) = {
    trades.find(_.id == id)
  }

  override def deleteById(id: String): Option[FXTrade] = {
    val ret = trades.find(_.id == id)
    trades = trades.filterNot(_.id == id)
    ret
  }

  override def findById(id: String) = {
    entities.find(_.id == id)
  }

  override def increment(tradingEntity: TradingEntity) = {
    findById(tradingEntity.id)
      .map(e => {
        val incrementd = e.copy(counter = e.counter + 1)
        entities = incrementd :: entities.filterNot(_.id == e.id)
        incrementd
      })
  }

  override def createTradingEntity(trader: TradingEntity): Option[TradingEntity] = {
    entities = trader :: entities
    Some(trader)
  }
}
