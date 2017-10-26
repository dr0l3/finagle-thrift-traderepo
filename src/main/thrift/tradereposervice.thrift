namespace java com.twitter.finagle.examples.names.thriftjava
#@namespace scala com.twitter.finagle.examples.names.thriftscala

struct FXTrade {
    1: string id
    2: string ccypair
    3: double rate
    4: string buyer
    5: string seller
}

exception MyCustomException {
    1: string message;
}

service TradeRepoService {
    FXTrade create(1: string ccypair, 2: double rate, 3: string buyer, 4: string seller)
    list<FXTrade> update(1: FXTrade updated)
    list<FXTrade> deleteById(1: string id)
    list<FXTrade> delete(1: FXTrade todelete)
    list<FXTrade> findById(1: string id)
    list<FXTrade> findByCcyPair(1: string ccypair)
    list<FXTrade> findByBuyer(1: string buyer)
    list<FXTrade> findBySeller(1: string seller)
}