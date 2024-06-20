package com.stocksapp.springwebflux.stock_trading.client;

import com.stocksapp.springwebflux.stock_trading.exception.ErrorMessage;
import com.stocksapp.springwebflux.stock_trading.exception.StockCreationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.stocksappapi.dto.CurrencyRate;
import org.stocksappapi.dto.StockPublishRequest;
import org.stocksappapi.dto.StockPublishResponse;
import reactor.core.publisher.Flux;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Slf4j
public class StockMarketClient{
    private WebClient webClient;

    public StockMarketClient(@Value("${clients.stockmarket.baseurl}") String baseUrl){
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .filter(ExchangeFilterFunction.ofRequestProcessor(
                        request -> Mono.just(ClientRequest.from(request)
                                .header("X-Trace-Id", UUID.randomUUID().toString())
                                .build())
                ))
                .build();
    }

    public Flux<CurrencyRate> getCurrencyRates(){
        return webClient.get()
                .uri("/currencyRates")
                .retrieve()
                .bodyToFlux(CurrencyRate.class)
                .doFirst(() -> log.info("Calling GET Currency Rates API"))
                .doOnNext(cr -> log.info("Get Currency Rates API Response: {}", cr));
    }

    public Mono<StockPublishResponse> publishStock(
            StockPublishRequest requestBody) {
        return webClient.post()
                .uri("/stocks/publish")
                .body(BodyInserters.fromValue(requestBody))
                .exchangeToMono(response ->
                        !response.statusCode().isError() ?
                                response.bodyToMono(StockPublishResponse.class) :
                                response.bodyToMono(ErrorMessage.class)
                                        .flatMap(errorMessage ->
                                                Mono.error(new StockCreationException(errorMessage.getMessage()))))
                .doFirst(() -> log.info("Calling Publish Stock API with Request Body: {}", requestBody))
                .doOnNext(spr -> log.info("Publish Stock API Response: {}", spr));
    }
}
