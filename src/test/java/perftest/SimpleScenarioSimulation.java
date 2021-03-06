package perftest;

import io.gatling.javaapi.core.ScenarioBuilder;

import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;

public class SimpleScenarioSimulation extends BasicSimulation {

    ScenarioBuilder simpleScenario = scenario("Create show and reserve seats")
            .feed(showIdsFeeder)
            .exec(http("create-show")
                    .post("/shows")
                    .body(createShowPayload)
            )
            .foreach(randomSeatNums.asJava(), "seatNum").on(
                    exec(http("reserve-seat")
                            .patch("shows/#{showId}/seats/#{seatNum}")
                            .body(reserveSeatPayload))
            );

    {
        setUp(simpleScenario.injectOpen(atOnceUsers(10)).protocols(httpProtocol));
    }
}
