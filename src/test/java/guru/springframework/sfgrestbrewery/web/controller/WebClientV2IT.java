package guru.springframework.sfgrestbrewery.web.controller;

import guru.springframework.sfgrestbrewery.web.functional.BeerRouterConfig;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import guru.springframework.sfgrestbrewery.web.model.BeerStyleEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class WebClientV2IT {
    public static final String BASE_URL = "http://localhost:8080";

    WebClient webClient;

    @BeforeEach
    void setUp() {
        webClient = WebClient.builder()
                .baseUrl(BASE_URL)
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().wiretap(true)))
                .build();
    }

    @Test
    void getBeerById() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        Mono<BeerDto> beerDtoMono = webClient.get().uri(BeerRouterConfig.BEER_V2_PATH + "/1")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(BeerDto.class);

        beerDtoMono.subscribe(beerDto -> {
            assertThat(beerDto).isNotNull();
            assertThat(beerDto.getBeerName()).isNotNull();

            countDownLatch.countDown();
        });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    void getBeerByIdNotFound() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);

        webClient.get().uri(BeerRouterConfig.BEER_V2_PATH + "/18755")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().toBodilessEntity()
                .subscribe(responseEntity -> {

                }, throwable -> {
                    if (throwable.getClass().getName().equals("org.springframework.web.reactive.function.client.WebClientResponseException$NotFound")) {
                        WebClientResponseException ex = (WebClientResponseException) throwable;

                        if (ex.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                            countDownLatch.countDown();
                        }
                    }
                });

        countDownLatch.countDown();
        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }


    @Test
    void getBeerByUpc() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        Mono<BeerDto> beerDtoMono = webClient.get().uri(BeerRouterConfig.BEER_UPC_V2_PATH + "/0631234200036")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(BeerDto.class);

        beerDtoMono.subscribe(beerDto -> {
            assertThat(beerDto).isNotNull();
            assertThat(beerDto.getBeerName()).isNotNull();

            countDownLatch.countDown();
        });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    void getBeerByUpcNotFound() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);

        webClient.get().uri(BeerRouterConfig.BEER_UPC_V2_PATH + "/987421")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().toBodilessEntity()
                .subscribe(responseEntity -> {
                }, throwable -> {
                    if (throwable.getClass().getName().equals("org.springframework.web.reactive.function.client.WebClientResponseException$NotFound")) {
                        WebClientResponseException ex = (WebClientResponseException) throwable;

                        if (ex.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                            countDownLatch.countDown();
                        }
                    }

                });

        countDownLatch.countDown();
        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    void saveBeer() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Random random = new Random();
        BeerDto beerDto = BeerDto.builder()
                .beerName("Estrella Damm")
                .beerStyle(BeerStyleEnum.LAGER.name())
                .upc("0631234211036")
                .price(new BigDecimal(BigInteger.valueOf(random.nextInt(10000)), 2))
                .quantityOnHand(random.nextInt(5000))
                .build();

        Mono<ResponseEntity<Void>> beerResponseMono = webClient.post().uri(BeerRouterConfig.BEER_V2_PATH)
                .accept(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(beerDto))
                .retrieve().toBodilessEntity();

        beerResponseMono.publishOn(Schedulers.parallel()).subscribe(responseEntity -> {

            assertThat(responseEntity.getStatusCode().is2xxSuccessful());

            countDownLatch.countDown();
        });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    void saveBeerBadRequest() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        Random random = new Random();
        BeerDto beerDto = BeerDto.builder()
                .beerStyle(BeerStyleEnum.LAGER.name())
                .upc("0631234211036")
                .price(new BigDecimal(BigInteger.valueOf(random.nextInt(10000)), 2))
                .quantityOnHand(random.nextInt(5000))
                .build();

        webClient.post().uri(BeerRouterConfig.BEER_V2_PATH)
                .accept(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(beerDto))
                .retrieve().toBodilessEntity()
                .subscribe(responseEntity -> {}, throwable -> {
                    if (throwable.getClass().getName().equals("org.springframework.web.reactive.function.client.WebClientResponseException$BadRequest")) {
                        WebClientResponseException ex = (WebClientResponseException) throwable;

                        if (ex.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
                            countDownLatch.countDown();
                        }
                    }

                });

        countDownLatch.countDown();
        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);

    }

    @Test
    void updateBeer() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(3);
        webClient.get().uri(BeerRouterConfig.BEER_V2_PATH + "/1")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(BeerDto.class)
                .subscribe(beerDto -> {
                    countDownLatch.countDown();

                    beerDto.setId(null);
                    beerDto.setBeerName("Updated Beer Name");

                    webClient.put().uri(BeerRouterConfig.BEER_V2_PATH+"/1")
                            .accept(MediaType.APPLICATION_JSON)
                            .body(BodyInserters.fromValue(beerDto))
                            .retrieve().toBodilessEntity()
                            .flatMap(responseEntity -> {
                                countDownLatch.countDown();
                                return webClient.get().uri(BeerRouterConfig.BEER_V2_PATH + "/1")
                                        .accept(MediaType.APPLICATION_JSON)
                                        .retrieve().bodyToMono(BeerDto.class);
                            }).subscribe(savedDTO -> {
                                countDownLatch.countDown();
                                assertThat(savedDTO.getBeerName()).isEqualTo(beerDto.getBeerName());
                            });
                });
        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    void updateBeerNotFound() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);

        BeerDto beerDto = BeerDto.builder()
                .beerName("Estrella Damm")
                .beerStyle(BeerStyleEnum.LAGER.name())
                .upc("0631234211036")
                .price(new BigDecimal("9.99"))
                .quantityOnHand(10)
                .build();

        webClient.put().uri(BeerRouterConfig.BEER_V2_PATH + "/87451")
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(beerDto))
                .retrieve().toBodilessEntity()
                .subscribe(responseEntity -> {
                }, throwable -> {
                    if (throwable.getClass().getName().equals("org.springframework.web.reactive.function.client.WebClientResponseException$NotFound")) {
                        WebClientResponseException ex = (WebClientResponseException) throwable;
                        if (ex.getStatusCode().equals(HttpStatus.NOT_FOUND))
                            countDownLatch.countDown();
                    }
                });
        countDownLatch.countDown();

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    void deleteBeer() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        webClient.delete().uri(BeerRouterConfig.BEER_V2_PATH + "/1")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().toBodilessEntity()
                .subscribe(responseEntity -> {
                    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
                    countDownLatch.countDown();
                });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    void deleteBeerNotFound() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);

        webClient.delete().uri(BeerRouterConfig.BEER_V2_PATH + "/78945")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().toBodilessEntity()
                .subscribe(responseEntity -> {
                }, throwable -> {
                    if (throwable.getClass().getName().equals("org.springframework.web.reactive.function.client.WebClientResponseException$NotFound")) {
                        WebClientResponseException ex = (WebClientResponseException) throwable;
                        if (ex.getStatusCode().equals(HttpStatus.NOT_FOUND))
                            countDownLatch.countDown();
                    }
                });
        countDownLatch.countDown();
        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }
}
