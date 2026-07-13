package com.coworking.reservation.config;

import com.coworking.reservation.config.properties.PaymentProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Configuration
public class PaymentClientConfig {

    @Bean
    public RestClient paymentRestClient(
            RestClient.Builder builder,
            PaymentProperties paymentProperties
    ){
        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        int timeoutInMillis = Math.toIntExact(
                paymentProperties.timeout().toMillis()
        );

        requestFactory.setConnectTimeout(timeoutInMillis);
        requestFactory.setReadTimeout(timeoutInMillis);

        return builder.
                baseUrl(paymentProperties.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }
}
