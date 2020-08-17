module no.ssb.concept {
    requires io.helidon.webserver;
    requires io.helidon.health;
    requires java.logging;
    requires io.helidon.health.checks;
    requires io.helidon.metrics;
    requires org.slf4j;
    requires jul.to.slf4j;
    requires logback.classic;
    requires io.helidon.media.jackson;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.module.paramnames;
    requires com.fasterxml.jackson.datatype.jdk8;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires io.helidon.webserver.accesslog;
    requires io.helidon.webclient;

    exports no.ssb.dapla.concept;
}