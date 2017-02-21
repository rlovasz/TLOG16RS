package com.rozsalovasz.tlog16rs;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import com.rozsalovasz.tlog16rs.entities.Task;
import com.rozsalovasz.tlog16rs.entities.User;
import com.rozsalovasz.tlog16rs.entities.WorkDay;
import com.rozsalovasz.tlog16rs.entities.WorkMonth;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.extern.slf4j.Slf4j;

/**
 * This class set up an EbeanServer to communicate with the database, and
 * updates the database through liquibase
 *
 * @author rlovasz
 */
@Slf4j
public class CreateDatabase {

    private EbeanServer ebeanServer;
    private DataSourceConfig dataSourceConfig;
    private ServerConfig serverConfig;

    /**
     * The constructor updates the database schema through liquibase and set up
     * the EbeanServer
     *
     * @param configuration contains the login informations to set up the
     * connection
     * @throws liquibase.exception.LiquibaseException
     * @throws java.lang.ClassNotFoundException
     * @throws java.sql.SQLException
     */
    public CreateDatabase(TLOG16RSConfiguration configuration) throws LiquibaseException, ClassNotFoundException, SQLException {
        updateSchema(configuration);
        initDataSourceConfig(configuration);
        initServerConfig(configuration);
        initEbeanServer();
    }

    private void initDataSourceConfig(TLOG16RSConfiguration configuration) {
        dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setDriver(configuration.getDbDriver());
        dataSourceConfig.setUrl(configuration.getDbUrl());
        dataSourceConfig.setUsername(configuration.getDbUsername());
        dataSourceConfig.setPassword(configuration.getDbPassword());

    }

    private void initServerConfig(TLOG16RSConfiguration configuration) {
        log.info("Setup ebean server");
        serverConfig = new ServerConfig();
        serverConfig.setName(configuration.getDbName());
        serverConfig.setDdlGenerate(false);
        serverConfig.setDdlRun(false);
        serverConfig.setRegister(true);
        serverConfig.setDataSourceConfig(dataSourceConfig);
        serverConfig.addClass(Task.class);
        serverConfig.addClass(WorkDay.class);
        serverConfig.addClass(WorkMonth.class);
        serverConfig.addClass(User.class);
        serverConfig.setDefaultServer(true);

    }

    private void initEbeanServer() {

        ebeanServer = EbeanServerFactory.create(serverConfig);
    }
    
    private Connection getConnection(TLOG16RSConfiguration configuration) throws SQLException {
        return DriverManager.getConnection(configuration.getDbUrl(), configuration.getDbUsername(),configuration.getDbPassword());
    }

    private void updateSchema(TLOG16RSConfiguration configuration) throws LiquibaseException, ClassNotFoundException, SQLException {
        final Connection connection = getConnection(configuration);
        Liquibase liquibase = new Liquibase(
                "migrations.xml",
                new ClassLoaderResourceAccessor(),
                new JdbcConnection(connection)
        );

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
