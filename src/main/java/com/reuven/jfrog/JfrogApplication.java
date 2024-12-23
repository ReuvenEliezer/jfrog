package com.reuven.jfrog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import reactor.core.publisher.Hooks;
import reactor.tools.agent.ReactorDebugAgent;

@ComponentScan(basePackages = {
        "com.reuven.jfrog.config",
        "com.reuven.jfrog.controllers",
        "com.reuven.jfrog.services"
})
@SpringBootApplication
public class JfrogApplication {

    public static void main(String[] args) {
        ReactorDebugAgent.init();
        Hooks.enableAutomaticContextPropagation(); //for tracing log in reactive
        SpringApplication.run(JfrogApplication.class, args);
    }

}
