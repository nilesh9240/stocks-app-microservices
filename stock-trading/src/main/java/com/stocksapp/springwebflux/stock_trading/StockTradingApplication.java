package com.stocksapp.springwebflux.stock_trading;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;

@SpringBootApplication()
public class StockTradingApplication {

	public static void main(String[] args) {
		SpringApplication.run(StockTradingApplication.class, args);
	}
	

}
