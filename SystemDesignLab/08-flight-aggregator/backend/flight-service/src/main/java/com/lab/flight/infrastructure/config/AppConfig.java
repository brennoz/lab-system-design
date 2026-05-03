package com.lab.flight.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lab.flight.application.saga.BookingFailedHandler;
import com.lab.flight.application.saga.CompleteBookingSaga;
import com.lab.flight.application.saga.ConfirmPaymentSaga;
import com.lab.flight.application.saga.HoldFlightSaga;
import com.lab.flight.application.usecase.CreateBookingUseCase;
import com.lab.flight.application.usecase.SearchFlightsUseCase;
import com.lab.flight.domain.port.BookingRepository;
import com.lab.flight.domain.port.FlightCachePort;
import com.lab.flight.domain.port.FlightProviderPort;
import com.lab.flight.domain.port.OutboxPort;
import com.lab.flight.infrastructure.cache.RedisFlightCacheAdapter;
import com.lab.flight.infrastructure.outbox.JpaOutboxRepository;
import com.lab.flight.infrastructure.outbox.SpringDataOutboxRepository;
import com.lab.flight.infrastructure.persistence.JpaBookingRepository;
import com.lab.flight.infrastructure.persistence.SpringDataBookingRepository;
import com.lab.flight.infrastructure.provider.ProviderAAdapter;
import com.lab.flight.infrastructure.provider.ProviderBAdapter;
import com.lab.flight.infrastructure.provider.ProviderCAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
@EnableScheduling
public class AppConfig {

    @Value("${wiremock.base-url}")
    private String wiremockBaseUrl;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public BookingRepository bookingRepository(SpringDataBookingRepository delegate) {
        return new JpaBookingRepository(delegate);
    }

    @Bean
    public OutboxPort outboxPort(SpringDataOutboxRepository delegate) {
        return new JpaOutboxRepository(delegate);
    }

    @Bean
    public FlightCachePort flightCachePort(StringRedisTemplate redis, ObjectMapper mapper) {
        return new RedisFlightCacheAdapter(redis, mapper);
    }

    @Bean
    public ProviderAAdapter providerA(RestTemplate restTemplate, ObjectMapper mapper) {
        return new ProviderAAdapter(restTemplate, wiremockBaseUrl, mapper);
    }

    @Bean
    public ProviderBAdapter providerB(RestTemplate restTemplate, ObjectMapper mapper) {
        return new ProviderBAdapter(restTemplate, wiremockBaseUrl, mapper);
    }

    @Bean
    public ProviderCAdapter providerC(RestTemplate restTemplate, ObjectMapper mapper) {
        return new ProviderCAdapter(restTemplate, wiremockBaseUrl, mapper);
    }

    @Bean
    public SearchFlightsUseCase searchFlightsUseCase(List<FlightProviderPort> providers,
                                                      FlightCachePort cache) {
        return new SearchFlightsUseCase(providers, cache);
    }

    @Bean
    public CreateBookingUseCase createBookingUseCase(BookingRepository bookingRepository,
                                                      OutboxPort outboxPort,
                                                      ObjectMapper mapper) {
        return new CreateBookingUseCase(bookingRepository, outboxPort, mapper);
    }

    @Bean
    public HoldFlightSaga holdFlightSaga(BookingRepository bookingRepository,
                                          List<FlightProviderPort> providers,
                                          KafkaTemplate<String, String> kafka) {
        return new HoldFlightSaga(bookingRepository, providers, kafka);
    }

    @Bean
    public ConfirmPaymentSaga confirmPaymentSaga(BookingRepository bookingRepository,
                                                  KafkaTemplate<String, String> kafka) {
        return new ConfirmPaymentSaga(bookingRepository, kafka);
    }

    @Bean
    public CompleteBookingSaga completeBookingSaga(BookingRepository bookingRepository,
                                                   KafkaTemplate<String, String> kafka) {
        return new CompleteBookingSaga(bookingRepository, kafka);
    }

    @Bean
    public BookingFailedHandler bookingFailedHandler(BookingRepository bookingRepository) {
        return new BookingFailedHandler(bookingRepository);
    }
}
