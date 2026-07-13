package com.coworking.reservation.config;

import com.coworking.reservation.config.properties.PaymentProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class PaymentClientConfig {

    @Bean
    public RestClient paymentRestClient(
            RestClient.Builder builder,
            PaymentProperties paymentProperties
    ){
        return builder.
                baseUrl(paymentProperties.baseUrl())
                .build();
    }
}
