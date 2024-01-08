/*
 * Copyright 2020 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.snowdrop.boot.narayana.core.jdbc;

import java.sql.SQLException;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import org.jboss.logging.Logger;

/**
 * XAResourceRecoveryHelper implementation which gets XIDs, which needs to be recovered, from the database.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class DataSourceXAResourceRecoveryHelper implements XAResourceRecoveryHelper, XAResource {

    private static final XAResource[] NO_XA_RESOURCES = {};

    private static final Logger logger = Logger.getLogger(DataSourceXAResourceRecoveryHelper.class);

    private final XADataSource xaDataSource;

    private final String user;

    private final String password;

    private XAConnection xaConnection;

    private XAResource delegate;

    /**
     * Create a new {@link DataSourceXAResourceRecoveryHelper} instance.
     *
     * @param xaDataSource the XA data source
     */
    public DataSourceXAResourceRecoveryHelper(XADataSource xaDataSource) {
        this(xaDataSource, null, null);
    }

    /**
     * Create a new {@link DataSourceXAResourceRecoveryHelper} instance.
     *
     * @param xaDataSource the XA data source
     * @param user         the database user or {@code null}
     * @param password     the database password or {@code null}
     */
    public DataSourceXAResourceRecoveryHelper(XADataSource xaDataSource, String user, String password) {
        this.xaDataSource = xaDataSource;
        this.user = user;
        this.password = password;
    }

    @Override
    public boolean initialise(String properties) {
        return true;
    }

    @Override
    public XAResource[] getXAResources() {
        if (connect()) {
            return new XAResource[]{ this };
        }
        return NO_XA_RESOURCES;
    }

    private boolean connect() {
        if (this.delegate == null) {
            try {
                this.xaConnection = getXaConnection();
                this.delegate = this.xaConnection.getXAResource();
            } catch (SQLException ex) {
                logger.warn("Failed to create connection", ex);
                return false;
            }
        }
        return true;
    }

    private XAConnection getXaConnection() throws SQLException {
        if (this.user == null && this.password == null) {
            return this.xaDataSource.getXAConnection();
        }
        return this.xaDataSource.getXAConnection(this.user, this.password);
    }

    @Override
    public Xid[] recover(int flag) throws XAException {
        try {
            return getDelegate().recover(flag);
        } finally {
            if (flag == XAResource.TMENDRSCAN) {
                disconnect();
            }
        }
    }

    private void disconnect() throws XAException {
        try {
            this.xaConnection.close();
        } catch (SQLException e) {
            logger.warn("Failed to close connection", e);
        } finally {
            this.xaConnection = null;
            this.delegate = null;
        }
    }

    @Override
    public void start(Xid xid, int flags) throws XAException {
        getDelegate().start(xid, flags);
    }

    @Override
    public void end(Xid xid, int flags) throws XAException {
        getDelegate().end(xid, flags);
    }

    @Override
    public int prepare(Xid xid) throws XAException {
        return getDelegate().prepare(xid);
    }

    @Override
    public void commit(Xid xid, boolean onePhase) throws XAException {
        getDelegate().commit(xid, onePhase);
    }

    @Override
    public void rollback(Xid xid) throws XAException {
        getDelegate().rollback(xid);
    }

    @Override
    public boolean isSameRM(XAResource xaResource) throws XAException {
        return getDelegate().isSameRM(xaResource);
    }

    @Override
    public void forget(Xid xid) throws XAException {
        getDelegate().forget(xid);
    }

    @Override
    public int getTransactionTimeout() throws XAException {
        return getDelegate().getTransactionTimeout();
    }

    @Override
    public boolean setTransactionTimeout(int seconds) throws XAException {
        return getDelegate().setTransactionTimeout(seconds);
    }

    private XAResource getDelegate() {
        if (this.delegate == null) {
            throw new IllegalStateException("Connection has not been opened");
        }
        return this.delegate;
    }

}
