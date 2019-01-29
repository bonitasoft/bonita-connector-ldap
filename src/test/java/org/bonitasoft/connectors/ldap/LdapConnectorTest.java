/**
 * Copyright (C) 2009-2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.connectors.ldap;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.exception.BonitaException;

import junit.framework.TestCase;

public class LdapConnectorTest extends TestCase {

    private LdapConnector connector;
    protected static final Logger LOG = Logger.getLogger(LdapConnectorTest.class.getName());

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (LdapConnectorTest.LOG.isLoggable(Level.WARNING)) {
            LdapConnectorTest.LOG.warning("======== Starting test: " + this.getClass().getName() + "." + this.getName() + "() ==========");
        }
    }

    @Override
    protected void tearDown() throws Exception {
        if (LdapConnectorTest.LOG.isLoggable(Level.WARNING)) {
            LdapConnectorTest.LOG.warning("======== Ending test: " + this.getName() + " ==========");
        }
        connector = null;
        super.tearDown();
    }

    private LdapConnector getBasicSettings() {
        connector = new LdapConnector();
        connector.setHost("localhost");
        connector.setProtocol(LdapProtocol.LDAP);
        connector.setBaseObject("ou=people,dc=bonita,dc=org");
        connector.setFilter("(cn=*o*)");
        return connector;
    }

    

    

    public void testValidate() {
        connector = new LdapConnector();
        try {
            connector.validateInputParameters();
            fail("Should get 4 errors");
        } catch (final ConnectorValidationException e) {

        }

        connector.setHost("mylocalhost");
        try {
            connector.validateInputParameters();
            fail("Should get 3 errors");
        } catch (final ConnectorValidationException e) {

        }

        connector.setProtocol(LdapProtocol.LDAP);
        try {
            connector.validateInputParameters();
            fail("Should get 2 errors");
        } catch (final ConnectorValidationException e) {

        }

        connector.setBaseObject("dc=mydomain,dc=com");
        try {
            connector.validateInputParameters();
            fail("Should get 1 error");
        } catch (final ConnectorValidationException e) {
        }

        connector.setFilter("(cn=Tom*)");
        // All required fields were set
        try {
            connector.validateInputParameters();
        } catch (final ConnectorValidationException e) {
            fail("Should not fail as all parameters are set");
        }

        connector.setUserName("What I want");
        connector.setPassword(null);
        try {
            connector.validateInputParameters();
            fail("Should get 1 error");
        } catch (final ConnectorValidationException e) {
        }

        connector.setUserName(null);
        connector.setPassword("What I want");
        try {
            connector.validateInputParameters();
            fail("Should get 1 error");
        } catch (final ConnectorValidationException e) {
        }

        connector.setUserName("");
        connector.setPassword("");
        try {
            connector.validateInputParameters();
        } catch (final ConnectorValidationException e) {
            fail("Should not fail as all parameters are set");
        }

        connector.setUserName("What I want");
        connector.setPassword("What I want");

        try {
            connector.validateInputParameters();
        } catch (final ConnectorValidationException e) {
            fail("Should not fail as all parameters are set");
        }

        connector.setUserName("What I want");
        connector.setPassword("pwd");
        try {
            connector.validateInputParameters();
        } catch (final ConnectorValidationException e) {
            fail("Should not fail as all parameters are set");
        }
    }

    public void testValidateValues() throws ConnectorValidationException {
        connector = getBasicSettings();
        connector.validateInputParameters();
    }

    public void testSetNullHost() {
        connector = getBasicSettings();
        connector.setHost(null);
        try {
            connector.validateInputParameters();
        } catch (final ConnectorValidationException e) {
            assertThat(e.getMessage(), containsString("host"));
        }
    }

    public void testSetEmptyHost() throws BonitaException {
        connector = getBasicSettings();
        connector.setHost("");
        try {
            connector.validateInputParameters();
        } catch (final ConnectorValidationException e) {
            assertThat(e.getMessage(), containsString("host"));
        }
    }

    public void testSetPortWithLessThanRange() throws BonitaException {
        connector = getBasicSettings();
        connector.setPort(-1);

        try {
            connector.validateInputParameters();
        } catch (final ConnectorValidationException e) {
            assertThat(e.getMessage(), containsString("port"));
        }
    }

    public void testSetPortWithGreaterThanRange() throws BonitaException {
        connector = getBasicSettings();
        connector.setPort(65536);

        try {
            connector.validateInputParameters();
        } catch (final ConnectorValidationException e) {
            assertThat(e.getMessage(), containsString("port"));
        }
    }

    public void testSetNullPort() throws BonitaException {
        connector = getBasicSettings();
        connector.setPort(null);

        try {
            connector.validateInputParameters();
        } catch (final ConnectorValidationException e) {
            assertThat(e.getMessage(), containsString("port"));
        }
    }

    public void testSetProtocol() {
        connector = getBasicSettings();
        connector.setProtocol("LDAP");
        assertEquals(LdapProtocol.LDAP, connector.getProtocol());
        connector.setProtocol("LDAPS");
        assertEquals(LdapProtocol.LDAPS, connector.getProtocol());
        connector.setProtocol("TLS");
        assertEquals(LdapProtocol.TLS, connector.getProtocol());

        connector.setProtocol("ldap");
        assertEquals(LdapProtocol.LDAP, connector.getProtocol());
        connector.setProtocol("ldaps");
        assertEquals(LdapProtocol.LDAPS, connector.getProtocol());
        connector.setProtocol("tls");
        assertEquals(LdapProtocol.TLS, connector.getProtocol());
    }

    public void testSetNullProtocol() throws BonitaException {
        connector = getBasicSettings();
        final LdapProtocol protocol = null;
        connector.setProtocol(protocol);

        try {
            connector.validateInputParameters();
        } catch (final ConnectorValidationException e) {
            assertThat(e.getMessage(), containsString("protocol"));
        }
    }

    public void testSetNullStringProtocol() throws ConnectorValidationException {
        connector = getBasicSettings();
        final String protocol = null;
        connector.setProtocol(protocol);
        connector.validateInputParameters();

        assertEquals(LdapProtocol.LDAPS, connector.getProtocol());
    }

    public void testSetBadProtocol() throws BonitaException {
        connector = getBasicSettings();
        connector.setProtocol("HTTP");

        connector.validateInputParameters();
        assertEquals(LdapProtocol.LDAPS, connector.getProtocol());
    }

    public void testSetBaseObject() {
        connector = getBasicSettings();
        connector.setBaseObject(null);

        try {
            connector.validateInputParameters();
            fail("Should fail");
        } catch (final ConnectorValidationException e) {
            assertThat(e.getMessage(), containsString("baseObject"));
        }
    }

    public void testSetScope() {
        connector = new LdapConnector();
        connector.setScope("BASE");
        assertEquals(LdapScope.BASE, connector.getScope());
        connector.setScope("ONELEVEL");
        assertEquals(LdapScope.ONELEVEL, connector.getScope());
        connector.setScope("SUBTREE");
        assertEquals(LdapScope.SUBTREE, connector.getScope());

        connector.setScope("base");
        assertEquals(LdapScope.BASE, connector.getScope());
        connector.setScope("onelevel");
        assertEquals(LdapScope.ONELEVEL, connector.getScope());
        connector.setScope("subtree");
        assertEquals(LdapScope.SUBTREE, connector.getScope());
    }

    public void testSetBadScope() throws BonitaException {
        connector = getBasicSettings();
        connector.setScope("ALLTREE");

        connector.validateInputParameters();
        assertEquals(LdapScope.ONELEVEL, connector.getScope());
    }

    public void testSetNullScope() {
        connector = getBasicSettings();
        final LdapScope scope = null;
        connector.setScope(scope);
        try {
            connector.validateInputParameters();
            fail("Should fail");
        } catch (final ConnectorValidationException e) {
            assertThat(e.getMessage(), containsString("scope"));
        }
    }

    public void testSetNullStringScope() throws ConnectorValidationException {
        connector = getBasicSettings();
        final String scope = null;
        connector.setScope(scope);
        connector.validateInputParameters();
        assertEquals(LdapScope.ONELEVEL, connector.getScope());
    }

    public void testSetNullAttributes() throws ConnectorValidationException {
        connector = getBasicSettings();
        final String[] attribs = null;
        connector.setAttributes(attribs);
        connector.validateInputParameters();
    }

    public void testSetNullSizeLimit() {
        connector = getBasicSettings();
        connector.setSizeLimit(null);
        try {
            connector.validateInputParameters();
            fail("Should fail");
        } catch (final ConnectorValidationException e) {
            assertThat(e.getMessage(), containsString("sizeLimit"));
        }
    }

    public void testSetNegativeLongSizeLimit() throws BonitaException {
        connector = getBasicSettings();
        connector.setSizeLimit(new Long(-1L));

        try {
            connector.validateInputParameters();
            fail("Should fail");
        } catch (final ConnectorValidationException e) {
            assertThat(e.getMessage(), containsString("sizeLimit"));
        }
    }

    public void testSetNegativeSizeLimit() throws BonitaException {
        connector = getBasicSettings();
        connector.setSizeLimit(-1L);

        try {
            connector.validateInputParameters();
            fail("Should fail");
        } catch (final ConnectorValidationException e) {
            assertThat(e.getMessage(), containsString("sizeLimit"));
        }
    }

    public void testSetNullFilter() throws BonitaException {
        connector = getBasicSettings();
        connector.setFilter(null);

        try {
            connector.validateInputParameters();
            fail("Should fail");
        } catch (final ConnectorValidationException e) {
            assertThat(e.getMessage(), containsString("filter"));
        }
    }

    

    

    
    

    public void testSetTimeLimitWithANegativeValue() {
        connector = getBasicSettings();
        connector.setTimeLimit(-4);

        try {
            connector.validateInputParameters();
            fail("Should fail");
        } catch (final ConnectorValidationException e) {
            assertThat(e.getMessage(), containsString("timeLimit"));
        }
    }

    public void testSetNullTimeLimit() throws BonitaException {
        connector = getBasicSettings();
        connector.setTimeLimit(null);

        try {
            connector.validateInputParameters();
            fail("Should fail");
        } catch (final ConnectorValidationException e) {
            assertThat(e.getMessage(), containsString("timeLimit"));
        }
    }

    public void testSetNullReferralHandling() throws BonitaException {
        connector = getBasicSettings();
        connector.setReferralHandling(null);

        try {
            connector.validateInputParameters();
            fail("Should fail");
        } catch (final ConnectorValidationException e) {
            assertThat(e.getMessage(), containsString("referralHandling"));
        }
    }

    public void testSetBadReferralHandling() {
        connector = getBasicSettings();
        connector.setReferralHandling("always");

        try {
            connector.validateInputParameters();
            fail("Should fail");
        } catch (final ConnectorValidationException e) {
            assertThat(e.getMessage(), containsString("referralHandling"));
        }

        connector = getBasicSettings();
        connector.setReferralHandling("IGNORE");

        try {
            connector.validateInputParameters();
            fail("Should fail");
        } catch (final ConnectorValidationException e) {
            assertThat(e.getMessage(), containsString("referralHandling"));
        }

        connector = getBasicSettings();
        connector.setReferralHandling("FOLLOW");
        try {
            connector.validateInputParameters();
            fail("Should fail");
        } catch (final ConnectorValidationException e) {
            assertThat(e.getMessage(), containsString("referralHandling"));
        }
    }

    public void testSetDereferencingAliasFromString() throws Exception {
        connector = getBasicSettings();
        connector.setDerefAliases("never");
        assertEquals(connector.getDerefAliases(), LdapDereferencingAlias.NEVER);
    }

    public void testSetReferralHandling() throws BonitaException {
        connector = getBasicSettings();
        connector.setReferralHandling("ignore");
        connector.validateInputParameters();
        connector.setReferralHandling("follow");
        connector.validateInputParameters();
    }

    

  /*public void testGetEmailFromUserIDWithSSLSupport() {
    connector = getSSLDirectoryServiceSettings();
    connector.setBaseObject("ou=people,dc=bonita,dc=org");
    connector.setFilter("(uid=doej)");
    connector.setAttributes(new String[] {"mail"});
    connector.setScope(LdapScope.SUBTREE);
    execute();
    assertEquals(1, connector.getLdapAttributeList().size());
    LdapAttribute expected = new LdapAttribute("mail", "john.doe@bonita.org");
    LdapAttribute actual = connector.getLdapAttributeList().get(0).get(0);
    assertEquals(expected.getName(), actual.getName());
    assertEquals(expected.getValue(), actual.getValue());
  }

  public void testGetEmailFromUserIDWithSSLSupportAndCertificate() throws Exception {
    connector = getSSLDirectoryServiceSettings();
    connector.setBaseObject("ou=people,dc=bonita,dc=org");
    connector.setFilter("(uid=doej)");
    connector.setAttributes(new String[] {"mail"});
    connector.setScope(LdapScope.SUBTREE);
    connector.setCertificatePath("E:\\truststore");
    connector.executeConnector();
    assertEquals(25, connector.getResult().length());
    String result = "mail" + Connector.KEY_VALUE_SEPARATOR
    + "john.doe@bonita.org";
    assertEquals(result, connector.getResult());
  }

  public void testGetEmailFromUserIDWithTLSSupport() {
    connector = getDirectoryServiceSettings();
    connector.setBaseObject("ou=people,dc=bonita,dc=org");
    connector.setFilter("(uid=doej)");
    connector.setProtocol(LdapProtocol.TLS);
    connector.setAttributes(new String[] {"mail"});
    connector.setScope(LdapScope.SUBTREE);
    execute();
    assertEquals(1, connector.getLdapAttributeList().size());
    LdapAttribute expected = new LdapAttribute("mail", "john.doe@bonita.org");
    LdapAttribute actual = connector.getLdapAttributeList().get(0).get(0);
    assertEquals(expected.getName(), actual.getName());
    assertEquals(expected.getValue(), actual.getValue());
  }

  public void testSearchWithAliasDereferencing() {
    //TODO
  }*/
}
