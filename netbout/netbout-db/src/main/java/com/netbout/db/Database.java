/**
 * Copyright (c) 2009-2011, netBout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code occasionally and without intent to use it, please report this
 * incident to the author by email.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.netbout.db;

import com.rexsl.core.Manifests;
import com.ymock.util.Logger;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

/**
 * Database-related utility class.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class Database {

    /**
     * Datasource to use.
     */
    private static DataSource source;

    /**
     * Reconnect on class initialization.
     */
    static {
        try {
            Database.reconnect();
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Private ctor.
     */
    private Database() {
        // intentionally empty
    }

    /**
     * Read next bout number.
     * @return Next bout number
     * @throws SQLException If some SQL error
     */
    public static Connection connection() throws SQLException {
        return Database.source.getConnection();
    }

    /**
     * Reconnect.
     * @throws SQLException If some SQL error
     */
    public static void reconnect() throws SQLException {
        Database.source = Database.datasource();
        Database.update();
    }

    /**
     * Create and return JDBC data source.
     * @return The data source
     */
    private static DataSource datasource() {
        final PoolableConnectionFactory factory = new PoolableConnectionFactory(
            Database.factory(),
            new GenericObjectPool(null),
            null,
            "SELECT name FROM identity WHERE name = ''",
            false,
            true
        );
        return new PoolingDataSource(factory.getPool());
    }

    /**
     * Create and return connection factory.
     * @return The connection factory
     */
    private static ConnectionFactory factory() {
        final long start = System.currentTimeMillis();
        final String driver = Manifests.read("Netbout-JdbcDriver");
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(ex);
        }
        final String url = Manifests.read("Netbout-JdbcUrl");
        final ConnectionFactory factory = new DriverManagerConnectionFactory(
            url,
            Manifests.read("Netbout-JdbcUser"),
            Manifests.read("Netbout-JdbcPassword")
        );
        Logger.info(
            Database.class,
            "#factory(): created with '%s' at '%s' [%dms]",
            driver,
            url,
            System.currentTimeMillis() - start
        );
        return factory;
    }

    /**
     * Update DB schema to the latest version.
     */
    private static void update() {
        final long start = System.currentTimeMillis();
        try {
            final Liquibase liquibase = new Liquibase(
                "com/netbout/db/liquibase.xml",
                new ClassLoaderResourceAccessor(),
                new JdbcConnection(Database.source.getConnection())
            );
            liquibase.update("netbout");
        } catch (liquibase.exception.LiquibaseException ex) {
            throw new IllegalStateException(ex);
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }
        Logger.info(
            Database.class,
            "#update(): updated DB schema [%dms]",
            System.currentTimeMillis() - start
        );
    }

}