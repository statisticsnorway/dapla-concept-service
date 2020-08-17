package no.ssb.dapla.concept;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.helidon.common.http.Http;
import io.helidon.config.Config;
import io.helidon.media.common.DefaultMediaSupport;
import io.helidon.media.jackson.JacksonSupport;
import io.helidon.webclient.WebClient;
import io.helidon.webclient.WebClientResponse;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public class ConceptService implements Service {

    private static final Logger LOG = LoggerFactory.getLogger(ConceptService.class);

    private static final ObjectMapper mapper = new ObjectMapper();
    private final int port;
    private final List<String> ldsSchemas;

    ConceptService(Config config) {
        port = config.get("concept-lds.port").asInt().get();
        ldsSchemas = config.get("concept-lds.schemas").asList(String.class).get();
    }

    @Override
    public void update(Routing.Rules rules) {
        rules.get("/{conceptResource}", this::httpGetConceptIds);
    }

    private void httpGetConceptIds(ServerRequest req, ServerResponse res) {
        String conceptResource = req.path().param("conceptResource");
        if(!ldsSchemas.contains(conceptResource)) {
            String msg = String.format("concept lds do not provide %s resource", conceptResource);
            LOG.warn(msg);
            res.status(Http.Status.NOT_FOUND_404).send(msg);
            return;
        }

        WebClient webClient = WebClient.builder()
                .baseUri("http://localhost:" + port + "/ns/" + conceptResource)
                .addMediaSupport(DefaultMediaSupport.create())
                .addMediaSupport(JacksonSupport.create(mapper))
                .build();

        WebClientResponse response = webClient.get().submit().toCompletableFuture().join();

        JsonNode body = response.content().as(JsonNode.class).toCompletableFuture().join();
        ArrayNode arrayOfIds = new ObjectMapper().createArrayNode();
        for (Iterator<JsonNode> it = body.elements(); it.hasNext(); ) {
            JsonNode node = it.next();
            arrayOfIds.add(node.get("id"));
        }
        res.status(Http.Status.OK_200).send(arrayOfIds);
    }
}
