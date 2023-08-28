package guru.springframework.sfgrestbrewery.web.functional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;


@Configuration
public class BeerRouterConfig {
    public static final String BEER_V2_PATH = "api/v2/beer";
    public static final String BEER_UPC_V2_PATH = "api/v2/beerUpc";

    @Bean
    public RouterFunction<ServerResponse> beerByIdRouterV2(BeerHandlerV2 beerHandler) {
        return route()
                .GET(BEER_V2_PATH + "/{beerId}", accept(MediaType.APPLICATION_JSON), beerHandler::getBeerById)
                .GET(BEER_UPC_V2_PATH + "/{upc}", accept(MediaType.APPLICATION_JSON), beerHandler::getBeerByUpc)
                .POST(BEER_V2_PATH, accept(MediaType.APPLICATION_JSON), beerHandler::saveBeer)
                .PUT(BEER_V2_PATH + "/{beerId}", accept(MediaType.APPLICATION_JSON), beerHandler::updateBeer)
                .DELETE(BEER_V2_PATH + "/{beerId}", accept(MediaType.APPLICATION_JSON), beerHandler::deleteBeer)
                .build();
    }
}
