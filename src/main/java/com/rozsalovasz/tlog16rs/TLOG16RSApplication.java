package com.rozsalovasz.tlog16rs;

import com.avaje.ebean.EbeanServer;
import com.rozsalovasz.tlog16rs.entities.TimeLogger;
import com.rozsalovasz.tlog16rs.resources.TLOG16RSResource;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.nashorn.internal.runtime.regexp.joni.constants.SyntaxProperties;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.JoseException;

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
	}

	@Override
	public void run(final TLOG16RSConfiguration configuration,
			final Environment environment) {
		final CreateDatabase database = new CreateDatabase(configuration);
		final EbeanServer ebeanServer = database.getEbeanServer();
		environment.jersey().register(new TLOG16RSResource());
	}

}
