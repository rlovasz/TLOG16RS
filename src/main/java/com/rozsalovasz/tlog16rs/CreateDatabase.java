package com.rozsalovasz.tlog16rs;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import java.sql.DriverManager;
import java.sql.SQLException;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.extern.slf4j.Slf4j;
import org.avaje.agentloader.AgentLoader;

/**
 * This class set up an EbeanServer to communicate with the database, and updates the database through liquibase
 *
 * @author rlovasz
 */
@Slf4j
public class CreateDatabase {

	private EbeanServer ebeanServer;
	private DataSourceConfig dataSourceConfig;
	private ServerConfig serverConfig;

	/**
	 * The constructor updates the database schema through liquibase and set up the EbeanServer
	 *
	 * @param configuration contains the login informations to set up the connection
	 */
	public CreateDatabase(TLOG16RSConfiguration configuration) {
		try {
			updateSchema(configuration);
		} catch (LiquibaseException | SQLException | ClassNotFoundException ex) {
			log.error(ex.getMessage());
		}
		agentLoader();
		setDataSourceConfig(configuration);
		setServerConfig(configuration);
		setEbeanServer();
	}

	private void agentLoader() {
		if (!AgentLoader.loadAgentFromClasspath("avaje-ebeanorm-agent", "debug=1;packages=com.rozsalovasz.tlog16rs.**")) {
			log.error("avaje-ebeanorm-agent not found in classpath - not dynamically loaded");
		}
	}

	private void setDataSourceConfig(TLOG16RSConfiguration configuration) {
		dataSourceConfig = new DataSourceConfig();
		dataSourceConfig.setDriver(configuration.getDbDriver());
		dataSourceConfig.setUrl(configuration.getDbUrl());
		dataSourceConfig.setUsername(configuration.getDbUsername());
		dataSourceConfig.setPassword(configuration.getDbPassword());

	}

	private void setServerConfig(TLOG16RSConfiguration configuration) {
		serverConfig = new ServerConfig();
		serverConfig.setName(configuration.getDbName());
		serverConfig.setDdlGenerate(false);
		serverConfig.setDdlRun(false);
		serverConfig.setRegister(true);
		serverConfig.setDataSourceConfig(dataSourceConfig);
		serverConfig.addPackage(configuration.getDbPackage());
		serverConfig.setDefaultServer(true);
		
	}

	private void setEbeanServer() {

		ebeanServer = EbeanServerFactory.create(serverConfig);
	}

	private void updateSchema(TLOG16RSConfiguration configuration) throws LiquibaseException, ClassNotFoundException, SQLException {

		Class.forName(configuration.getDbDriver());
		Liquibase liquibase = new Liquibase("migrations.xml",
				new ClassLoaderResourceAccessor(),
				new JdbcConnection(DriverManager.getConnection(
						configuration.getDbUrl(),
						configuration.getDbUsername(),
						configuration.getDbPassword())));
		liquibase.update(new Contexts());

	}

	/**
	 *
	 * @return with the set up EbeanServer object
	 */
	public EbeanServer getEbeanServer() {

		return ebeanServer;
	}

}
