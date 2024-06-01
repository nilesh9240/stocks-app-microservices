package org.stocksapp.service;

import org.springframework.stereotype.Service;


import org.stocksapp.exception.StockPublishingException;
import org.stocksappapi.dto.StockPublishRequest;
import org.stocksappapi.dto.StockPublishResponse;
import reactor.core.publisher.Mono;

@Service
public class StockPublishingService {

    public Mono<StockPublishResponse> publishStock(StockPublishRequest request) {
        return Mono.just(request)
            .map(this::persistStock);
    }

    private StockPublishResponse persistStock(StockPublishRequest request) {
        return StockPublishResponse.builder()
                .amount(request.getPrice())
                .stockName(request.getStockName())
                .currencyName(request.getCurrencyName())
                .status(getStatus(request))
                .build();
    }

    private String getStatus(StockPublishRequest request) {
        if(request.getStockName().contains("-"))
            throw new StockPublishingException("Stock name contains illegal character '-': ");
        return  "SUCCESS";
    }
}
