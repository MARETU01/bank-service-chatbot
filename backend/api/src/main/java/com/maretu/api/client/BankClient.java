package com.maretu.api.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "bank-service")
public interface BankClient {

}
