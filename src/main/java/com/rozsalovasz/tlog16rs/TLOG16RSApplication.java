package com.rozsalovasz.tlog16rs;

import com.avaje.ebean.EbeanServer;
import com.rozsalovasz.tlog16rs.resources.TLOG16RSResource;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class TLOG16RSApplication extends Application<TLOG16RSConfiguration> {

	public static void main(final String[] args) throws Exception {
		new TLOG16RSApplication().run(args);
    }

    @Override
    public String getName() {
        return "TLOG16RS";
    }

    @Override
    public void initialize(final Bootstrap<TLOG16RSConfiguration> bootstrap) {
		 bootstrap.addBundle(new AssetsBundle("/pages/", "/time/", "index.html", "static"));
    }

    @Override
    public void run(final TLOG16RSConfiguration configuration,
			final Environment environment) {
		final CreateDatabase database = new CreateDatabase(configuration);
		final EbeanServer ebeanServer = database.getEbeanServer();
        environment.jersey().register(new TLOG16RSResource());
    }

}
