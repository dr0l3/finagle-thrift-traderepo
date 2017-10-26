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

