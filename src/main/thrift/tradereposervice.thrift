struct TradingEntity {
    1: string id
    2: i32 counter
}


struct FXTrade {
    1: string id
    2: string ccypair
    3: double rate
    4: TradingEntity buyer
    5: TradingEntity seller
}




exception AppException {
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
    2: AppException error
}

service TradeService {
    TradingEntity findByName(1: string name) throws(1: NotFoundException nfe)
    TradingEntity createTrader(1: string name) throws(1: AppException mce)
    FXTrade create(1: string ccypair, 2: double rate, 3: TradingEntity buyer, 4: TradingEntity seller) throws(1: RedisException re, 2: AppException ce)
    FXTrade update(1: FXTrade updated)
    FXTrade deleteById(1: string id)
    FXTrade delete(1: FXTrade todelete)
    FXTrade findById(1: string id) throws(1: NotFoundException nfe)
//    list<FXTrade> findByCcyPair(1: string ccypair)
//    list<FXTrade> findByBuyer(1: string buyer)
//    list<FXTrade> findBySeller(1: string seller)
}