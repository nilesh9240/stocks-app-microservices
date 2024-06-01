package org.stocksapp.controller;


import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.stocksapp.service.CurrencyRatesService;
import org.stocksapp.service.StockPublishingService;
import org.stocksappapi.dto.CurrencyRate;
import org.stocksappapi.dto.StockPublishRequest;
import org.stocksappapi.dto.StockPublishResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
public class StockMarketController {

    private CurrencyRatesService currencyRatesService;
    private StockPublishingService stockPublishingService;

    @GetMapping("/currencyRates")
    public Flux<CurrencyRate> getCurrencyRates() {
        return currencyRatesService.getCurrencyRates();
    }

    @PostMapping("/stocks/publish")
    public Mono<StockPublishResponse> publishStock(@RequestBody StockPublishRequest request) {
        return stockPublishingService.publishStock(request);
    }
}
