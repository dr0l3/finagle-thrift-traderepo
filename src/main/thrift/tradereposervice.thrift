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

exception RedisException {
    1: string message
}

exception NotFoundException {
    1: string message
}

union FxRepoResult {
    1: FXTrade trade
    2: MyCustomException error
}

service TradeRepoService {
    FXTrade create(1: string ccypair, 2: double rate, 3: string buyer, 4: string seller)
    throws(1: RedisException re, 2: MyCustomException ce)
    FXTrade update(1: FXTrade updated)
    FXTrade deleteById(1: string id)
    FXTrade delete(1: FXTrade todelete)
    FXTrade findById(1: string id)
//    list<FXTrade> findByCcyPair(1: string ccypair)
//    list<FXTrade> findByBuyer(1: string buyer)
//    list<FXTrade> findBySeller(1: string seller)
}