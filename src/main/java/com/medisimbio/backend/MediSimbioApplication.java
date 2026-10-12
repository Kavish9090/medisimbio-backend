package com.medisimbio.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * MediSimbio Backend Application Entrypoint.
 * Modular monolith foundation providing core services for the MediSimbio healthcare ecosystem.
 */
@SpringBootApplication
public class MediSimbioApplication {

    private static final Logger log = LoggerFactory.getLogger(MediSimbioApplication.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(MediSimbioApplication.class);
        Environment env = app.run(args).getEnvironment();
        logApplicationStartup(env);
    }

    private static void logApplicationStartup(Environment env) {
        String protocol = "http";
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }
        String serverPort = env.getProperty("server.port", "8081");
        String contextPath = env.getProperty("server.servlet.context-path", "");
        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("The host name could not be determined, using `localhost` as fallback");
        }

        log.info("""

            --------------------------------------------------------------------------------
            \tApplication '{}' is running!
            \tLocal: \t\t{}://localhost:{}{}
            \tExternal: \t{}://{}:{}{}
            \tProfile(s): \t{}
            \tSwagger UI: \t{}://localhost:{}{}/swagger-ui.html
            \tHealth API: \t{}://localhost:{}{}/api/v1/health
            --------------------------------------------------------------------------------
            """,
            env.getProperty("spring.application.name", "medisimbio-backend"),
            protocol, serverPort, contextPath,
            protocol, hostAddress, serverPort, contextPath,
            env.getActiveProfiles().length == 0 ? "default" : String.join(", ", env.getActiveProfiles()),
            protocol, serverPort, contextPath,
            protocol, serverPort, contextPath
        );
    }
}
