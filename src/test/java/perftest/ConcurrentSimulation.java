package perftest;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.vavr.collection.List;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;

public class ConcurrentSimulation extends BasicSimulation {

    static int howManyShows = 100;
    private static List<String> showIds = List.range(0, howManyShows)
            .map(__ -> UUID.randomUUID().toString());


    Iterator<Map<String, Object>> showIdsFeeder = showIds.map(showId -> Collections.<String, Object>singletonMap("showId", showId)).iterator();
    Iterator<Map<String, Object>> reservationsFeeder = showIds
            .flatMap(showId -> List.range(0, maxSeats)
                    .map(seatNum -> Map.<String, Object>of("showId", showId, "seatNum", seatNum))
            )
            .shuffle()
            .iterator();

    ScenarioBuilder createShows = scenario("Create show scenario")
            .feed(showIdsFeeder)
            .exec(http("create-show")
                    .post("/shows")
                    .body(createShowPayload)
            );

    ScenarioBuilder reserveSeats = scenario("Reserve seats")
            .feed(reservationsFeeder)
            .exec(http("reserve-seat")
                    .patch("shows/#{showId}/seats/#{seatNum}")
                    .body(reserveSeatPayload));

    {
        setUp(createShows.injectOpen(atOnceUsers(howManyShows)).andThen(
                reserveSeats.injectOpen(constantUsersPerSec(10).during(60))))
                .protocols(httpProtocol);
    }
}
