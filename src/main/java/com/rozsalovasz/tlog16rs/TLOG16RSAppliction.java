package com.rozsalovasz.tlog16rs;

import com.rozsalovasz.tlog16rs.resources.TLOG16RSResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class TLOG16RSAppliction extends Application<TLOG16RSConfiguration> {

    public static void main(final String[] args) throws Exception {
        new TLOG16RSAppliction().run(args);
    }

    @Override
    public String getName() {
        return "TLOG16RS";
    }

    @Override
    public void initialize(final Bootstrap<TLOG16RSConfiguration> bootstrap) {
        // TODO: application initialization
    }

    @Override
    public void run(final TLOG16RSConfiguration configuration,
                    final Environment environment) {
        environment.jersey().register(new TLOG16RSResource());
    }

}
