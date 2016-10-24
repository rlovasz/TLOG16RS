package com.rozsalovasz.tlog16rs;

import io.dropwizard.Configuration;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

@Getter
@Setter
public class TLOG16RSConfiguration extends Configuration {

	@NotEmpty
	protected String dbDriver;

	@NotEmpty
	protected String dbUrl;

	@NotEmpty
	protected String dbUsername;

	@NotEmpty
	protected String dbPassword;

	@NotEmpty
	protected String dbName;
}
