package perftest;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.gatling.javaapi.core.Body;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import io.vavr.collection.List;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.http.HttpDsl.http;

abstract class BasicSimulation extends Simulation {

    private Config config = ConfigFactory.load();
    private Config simulationConfig = config.getConfig("simulation-config");
    private String baseUrl = simulationConfig.getString("base-url");
    int maxSeats = simulationConfig.getInt("max-seats");
    int usersPerSec = simulationConfig.getInt("users-per-sec");
    int duringSec = simulationConfig.getInt("during-sec");
    List<Integer> randomSeatNums = List.range(0, maxSeats).shuffle();

    HttpProtocolBuilder httpProtocol = http
            .baseUrl(baseUrl)
            .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .acceptEncodingHeader("gzip, deflate")
            .acceptLanguageHeader("en-US,en;q=0.5")
            .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")
            .header("accept", "application/json")
            .header("content-type", "application/json");


    Iterator<Map<String, Object>> showIdsFeeder =
            Stream.generate((Supplier<Map<String, Object>>) () -> {
                        return Collections.singletonMap("showId", UUID.randomUUID().toString());
                    }
            ).iterator();


    Body createShowPayload = StringBody(session -> {
        var showId = session.getString("showId");
        return String.format("""
                {
                  "showId": "%s",
                  "title": "show title %s",
                  "maxSeats": %s
                }
                """, showId, showId.substring(0, 8), maxSeats);
    });

    Body reserveSeatPayload = StringBody("""
            {
              "action": "RESERVE"
            }
            """);
}
