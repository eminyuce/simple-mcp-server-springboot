package com.yuce.mcp.feign;


import com.yuce.mcp.config.FeignClientConfig;
import com.yuce.mcp.model.StockResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "stockClient",
        url = "${feign.stock.client.url}",
        configuration = FeignClientConfig.class)
public interface StockClient {

    @GetMapping("/stock")
    ResponseEntity<StockResponse> getStockPrice(
            @RequestParam("symbol") String symbol
    );
}