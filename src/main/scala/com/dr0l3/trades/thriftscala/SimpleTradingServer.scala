package com.dr0l3.trades.thriftscala

import com.twitter.util.Future
import thrift.{PriceHistory, Strategy, TradingBotService}

class SimpleTradingServer extends TradingBotService[Future]{
  override def simulate(strategy: Strategy, history: PriceHistory) = ???

  override def fetch() = ???
}
