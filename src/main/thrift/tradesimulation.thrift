struct Holding {
    1: string id
    2: string symbol
    3: double volume
}

struct Signal {
    1: double price
}

struct Selector {
    1: string symbol
}

struct Strategy {
    1: string id
    2: Signal buysignal
    3: Signal sellsignal
    4: Selector selector
    5: i16 priority
    6: list<Holding> holdings
}

struct SimResult {
    1: double capital
    2: list<Holding> holdings
}

struct PriceDataPoint{
    1: string symbol
    2: string date
    3: double open
    4: double high
    5: double low
    6: double close
    7: i64 volume
}

struct PriceHistory {
    1: map<string,PriceDataPoint> datapoints
}

service TradingBotService {
    SimResult simulate(1: Strategy strategy, 2: PriceHistory history)
    PriceHistory fetch()
}

