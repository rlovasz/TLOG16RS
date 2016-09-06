package com.mycompany.helloworldmaven;

import com.mycompany.helloworldmaven.resources.HelloResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class HelloWorldMavenApplication extends Application<HelloWorldMavenConfiguration> {

    public static void main(final String[] args) throws Exception {
        new HelloWorldMavenApplication().run(args);
    }

    @Override
    public String getName() {
        return "HelloWorldMaven";
    }

    @Override
    public void initialize(final Bootstrap<HelloWorldMavenConfiguration> bootstrap) {
        // TODO: application initialization
    }

    @Override
    public void run(final HelloWorldMavenConfiguration configuration,
                    final Environment environment) {
        environment.jersey().register(new HelloResource());
    }

}
