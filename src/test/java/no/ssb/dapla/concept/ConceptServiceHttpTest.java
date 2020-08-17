package no.ssb.dapla.concept;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.helidon.common.http.Http;
import io.helidon.config.Config;
import io.helidon.media.common.DefaultMediaSupport;
import io.helidon.media.jackson.JacksonSupport;
import io.helidon.webclient.WebClient;
import io.helidon.webclient.WebClientResponse;
import io.helidon.webserver.WebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static io.helidon.config.ConfigSources.classpath;

class ConceptServiceHttpTest {

    private static final Logger LOG = LoggerFactory.getLogger(ConceptServiceHttpTest.class);

    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new ParameterNamesModule())
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule());

    private static WebServer webServer;

    @BeforeAll
    public static void startTheServer() {
        Config config = Config
                .builder(classpath("application-dev.yaml"),
                        classpath("application.yaml"))
                .metaConfig()
                .build();
        long webServerStart = System.currentTimeMillis();
        webServer = new ConceptApplication(config).get(WebServer.class);
        webServer.start().toCompletableFuture()
                .thenAccept(webServer -> {
                    long duration = System.currentTimeMillis() - webServerStart;
                    LOG.info("Server started in {} ms, listening at port {}", duration, webServer.port());
                })
                .orTimeout(5, TimeUnit.SECONDS)
                .join();
    }

    @AfterAll
    public static void stopServer() {
        if (webServer != null) {
            webServer.shutdown()
                    .toCompletableFuture()
                    .orTimeout(10, TimeUnit.SECONDS)
                    .join();
        }
    }

    @Test
    @Disabled()
    public void thatWeCanReadRepresentedVariable() {
        WebClient webClient = WebClient.builder()
                .baseUri("http://localhost:" + webServer.port())
                .addMediaSupport(DefaultMediaSupport.create())
                .addMediaSupport(JacksonSupport.create(mapper))
                .build();

        WebClientResponse response = webClient.get()
                .path("/concept/RepresentedVariable")
                .submit().toCompletableFuture().join();

        String body = response.content().as(String.class).toCompletableFuture().join();
        System.out.println(body);

        Http.ResponseStatus status = response.status();
        System.out.println(status);
    }
}
