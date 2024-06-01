package com.stocksapp.springwebflux.stock_trading.service;

import com.stocksapp.springwebflux.stock_trading.client.StockMarketClient;
import com.stocksapp.springwebflux.stock_trading.dto.StockRequest;
import com.stocksapp.springwebflux.stock_trading.dto.StockResponse;
import com.stocksapp.springwebflux.stock_trading.exception.StockCreationException;
import com.stocksapp.springwebflux.stock_trading.exception.StockNotFoundException;
import com.stocksapp.springwebflux.stock_trading.model.Stock;
import com.stocksapp.springwebflux.stock_trading.repository.StocksRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.stocksappapi.dto.StockPublishRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
public class StocksService {

    private static final Logger log = LoggerFactory.getLogger(StocksService.class);

    @Autowired
    private StocksRepository stocksRepository;

    @Autowired
    private StockMarketClient stockMarketClient;

    public Mono<StockResponse> getOneStock(String id, String currency){
        return stocksRepository.findById(id)
                .flatMap(stock -> stockMarketClient.getCurrencyRates()
                        .filter(currencyRate -> currency.equalsIgnoreCase(
                                currencyRate.getCurrencyName()))
                        .singleOrEmpty()
                        .map(currencyRate -> StockResponse.builder()
                                .id(stock.getId())
                                .name(stock.getName())
                                .currency(currencyRate.getCurrencyName())
                                .price(stock.getPrice().multiply(currencyRate.getRate()))
                                .build()))
                .switchIfEmpty(Mono.error(
                        new StockNotFoundException(
                                "Stock not found with id: " + id)))
                .doFirst(() -> log.info("Retrieving stock with id: {}", id))
                .doOnNext(stock -> log.info("Stock found: {}", stock))
                .doOnError(ex -> log.error("Something went wrong while retrieving the stock with id: {}", id, ex))
                .doOnTerminate(() -> log.info("Finalized retrieving stock"))
                .doFinally(signalType -> log.info("Finalized retrieving stock with signal type: {}", signalType));
    }

    public Flux<StockResponse> getAllStocks(BigDecimal priceGreaterThan) {
        return stocksRepository.findAll()
                .filter(stock -> stock.getPrice().compareTo(priceGreaterThan) > 0)
                .map(StockResponse::fromModel)
                .doFirst(() -> log.info("Retrieving all stocks"))
                .doOnNext(stock -> log.info("Stock found: {}", stock))
                .doOnError(ex -> log.error("Something went wrong happened while retrieving stocks", ex))
                .doOnTerminate(() -> log.info("Finalized retrieving stock"))
                .doFinally(signalType -> log.info("Finished stock retrieval with signal type: {}", signalType));


    }


    public Mono<StockResponse> createStock(StockRequest stockRequest) {
        return Mono.just(stockRequest)
                .map(StockRequest::toModel)
                .flatMap(stock -> stocksRepository.save(stock))
                .flatMap(stock -> stockMarketClient.publishStock(generateStockPublishRequest(stockRequest))
                        .filter(stockPublishResponse ->
                                "SUCCESS".equalsIgnoreCase(stockPublishResponse.getStatus()))
                        .map(stockPublishResponse ->  StockResponse.fromModel(stock))
                        .switchIfEmpty(Mono.error(
                                new StockCreationException("Unable to publish stock to the Stock Market"))))
                .onErrorMap(ex -> new StockCreationException(ex.getMessage()));
    }

    private StockPublishRequest generateStockPublishRequest(StockRequest stockRequest) {
        return StockPublishRequest.builder()
                .stockName(stockRequest.getName())
                .price(stockRequest.getPrice())
                .currencyName(stockRequest.getCurrency())
                .build();
    }
}
