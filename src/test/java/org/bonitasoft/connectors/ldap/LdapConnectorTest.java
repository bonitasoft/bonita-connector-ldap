/*
 * Copyright (C) 2009 - 2020 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.bonitasoft.connectors.ldap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import org.assertj.core.api.Assertions;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.exception.BonitaException;
import org.junit.jupiter.api.Test;

class LdapConnectorTest {

    private LdapConnector getBasicSettings() {
        LdapConnector connector = new LdapConnector();
        connector.setHost("localhost");
        connector.setProtocol(LdapProtocol.LDAP);
        connector.setBaseObject("ou=people,dc=bonita,dc=org");
        connector.setFilter("(cn=*o*)");
        return connector;
    }

    @Test
    void testValidateMandatoryParameters() {
        LdapConnector connector = new LdapConnector();

        ConnectorValidationException exception = assertThrows(ConnectorValidationException.class,
                () -> connector.validateInputParameters());
        assertThat(exception).hasMessageContaining("host cannot be empty!")
                .hasMessageContaining("baseObject cannot be empty!")
                .hasMessageContaining("filter cannot be empty!")
                .hasMessageContaining("protocol cannot be null");
    }
    
    @Test
    void testMissingPassword() throws ConnectorValidationException {
        LdapConnector connector = getBasicSettings();
        connector.setUserName(null);
        connector.setPassword("What I want");
        
        ConnectorValidationException exception = assertThrows(ConnectorValidationException.class,
                () -> connector.validateInputParameters());
        assertThat(exception).hasMessageContaining("username cannot be empty!");
    }
    
    @Test
    void testMissingUsername() throws ConnectorValidationException {
        LdapConnector connector = getBasicSettings();
        connector.setUserName("What I want");
        connector.setPassword("pwd");
        assertDoesNotThrow(() -> connector.validateInputParameters());
    }
    
    @Test
    void testValidUsernameAndPassword() throws ConnectorValidationException {
        LdapConnector connector = getBasicSettings();
        connector.setUserName("What I want");
        connector.setPassword(null);
        
        ConnectorValidationException exception = assertThrows(ConnectorValidationException.class,
                () -> connector.validateInputParameters());
        assertThat(exception).hasMessageContaining("password cannot be empty!");
    }

    @Test
    void testValidInputs() throws ConnectorValidationException {
        LdapConnector connector = getBasicSettings();
        connector.validateInputParameters();
    }

    @Test
    void testSetNullHost() {
        LdapConnector connector = getBasicSettings();
        connector.setHost(null);
        try {
            connector.validateInputParameters();
        } catch (final ConnectorValidationException e) {
            assertThat(e).hasMessageContaining("host cannot be empty!");
        }
    }

    @Test
    void testSetEmptyHost() throws BonitaException {
        LdapConnector connector = getBasicSettings();
        connector.setHost("");
        try {
            connector.validateInputParameters();
        } catch (final ConnectorValidationException e) {
            assertThat(e).hasMessageContaining("host cannot be empty!");
        }
    }

    @Test
    void testSetPortWithLessThanRange() throws BonitaException {
        LdapConnector connector = getBasicSettings();
        connector.setPort(-1);

        try {
            connector.validateInputParameters();
        } catch (final ConnectorValidationException e) {
            assertThat(e).hasMessageContaining("port cannot be less than 0!");
        }
    }

    @Test
    void testSetPortWithGreaterThanRange() throws BonitaException {
        LdapConnector connector = getBasicSettings();
        connector.setPort(65536);

        try {
            connector.validateInputParameters();
        } catch (final ConnectorValidationException e) {
            assertThat(e).hasMessageContaining("port cannot be greater than 65535!");
        }
    }

    @Test
    void testSetNullPort() throws BonitaException {
        LdapConnector connector = getBasicSettings();
        connector.setPort(null);

        try {
            connector.validateInputParameters();
        } catch (final ConnectorValidationException e) {
            assertThat(e).hasMessageContaining("port cannot be less than 0!");
        }
    }

    @Test
    void testSetProtocol() {
        LdapConnector connector = getBasicSettings();
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

    @Test
    void testSetNullProtocol() throws BonitaException {
        LdapConnector connector = getBasicSettings();
        connector.setProtocol((LdapProtocol) null);

        try {
            connector.validateInputParameters();
        } catch (final ConnectorValidationException e) {
            assertThat(e).hasMessageContaining("protocol cannot be null");
        }
    }

    @Test
    void testSetNullStringProtocol() throws ConnectorValidationException {
        LdapConnector connector = getBasicSettings();
        final String protocol = null;
        connector.setProtocol(protocol);
        connector.validateInputParameters();

        assertEquals(LdapProtocol.LDAPS, connector.getProtocol());
    }

    @Test
    void testSetBadProtocol() throws BonitaException {
        LdapConnector connector = getBasicSettings();
        connector.setProtocol("HTTP");

        connector.validateInputParameters();
        assertEquals(LdapProtocol.LDAPS, connector.getProtocol());
    }

    @Test
    void testSetBaseObject() {
        LdapConnector connector = getBasicSettings();
        connector.setBaseObject(null);

        try {
            connector.validateInputParameters();
            fail("Should fail");
        } catch (final ConnectorValidationException e) {
            assertThat(e).hasMessageContaining("baseObject cannot be empty!");
        }
    }

    @Test
    void testSetScope() {
        LdapConnector connector = new LdapConnector();
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

    @Test
    void testSetBadScope() throws BonitaException {
        LdapConnector connector = getBasicSettings();
        connector.setScope("ALLTREE");

        connector.validateInputParameters();
        assertEquals(LdapScope.ONELEVEL, connector.getScope());
    }

    @Test
    void testSetNullScope() {
        LdapConnector connector = getBasicSettings();
        connector.setScope((LdapScope) null);
        try {
            connector.validateInputParameters();
            fail("Should fail");
        } catch (final ConnectorValidationException e) {
            assertThat(e).hasMessageContaining("scope cannot be null");
        }
    }

    @Test
    void testSetNullStringScope() throws ConnectorValidationException {
        LdapConnector connector = getBasicSettings();
        final String scope = null;
        connector.setScope(scope);
        connector.validateInputParameters();
        assertEquals(LdapScope.ONELEVEL, connector.getScope());
    }

    @Test
    void testSetNullAttributes() throws ConnectorValidationException {
        LdapConnector connector = getBasicSettings();
        final String[] attribs = null;
        connector.setAttributes(attribs);
        connector.validateInputParameters();
    }

    @Test
    void testSetAttributes() throws ConnectorValidationException {
        LdapConnector connector = getBasicSettings();
        connector.setAttributes("dn,objectclass ,ou");
        connector.validateInputParameters();

        Assertions.assertThat(connector.getAttributes()).contains("dn", "objectclass", "ou");
    }

    @Test
    void testSetNullSizeLimit() {
        LdapConnector connector = getBasicSettings();
        connector.setSizeLimit(null);
        try {
            connector.validateInputParameters();
            fail("Should fail");
        } catch (final ConnectorValidationException e) {
            assertThat(e).hasMessageContaining("sizeLimit cannot be null or negative");
        }
    }

    @Test
    void testSetNegativeLongSizeLimit() throws BonitaException {
        LdapConnector connector = getBasicSettings();
        connector.setSizeLimit(-1L);

        try {
            connector.validateInputParameters();
            fail("Should fail");
        } catch (final ConnectorValidationException e) {
            assertThat(e).hasMessageContaining("sizeLimit cannot be null or negative");
        }
    }

    @Test
    void testSetNullFilter() throws BonitaException {
        LdapConnector connector = getBasicSettings();
        connector.setFilter(null);

        try {
            connector.validateInputParameters();
            fail("Should fail");
        } catch (final ConnectorValidationException e) {
            assertThat(e).hasMessageContaining("filter cannot be empty!");
        }
    }

    @Test
    void testSetTimeLimitWithANegativeValue() {
        LdapConnector connector = getBasicSettings();
        connector.setTimeLimit(-4);

        try {
            connector.validateInputParameters();
            fail("Should fail");
        } catch (final ConnectorValidationException e) {
            assertThat(e).hasMessageContaining("timeLimit cannot be null or negative");
        }
    }

    void testSetNullTimeLimit() throws BonitaException {
        LdapConnector connector = getBasicSettings();
        connector.setTimeLimit(null);

        try {
            connector.validateInputParameters();
            fail("Should fail");
        } catch (final ConnectorValidationException e) {
            assertThat(e).hasMessageContaining("timeLimit cannot be null or negative");
        }
    }

    @Test
    void testSetNullReferralHandling() throws BonitaException {
        LdapConnector connector = getBasicSettings();
        connector.setReferralHandling(null);

        try {
            connector.validateInputParameters();
            fail("Should fail");
        } catch (final ConnectorValidationException e) {
            assertThat(e).hasMessageContaining("referralHandling is null!");
        }
    }

    @Test
    void testSetBadReferralHandling() {
        LdapConnector connector = getBasicSettings();
        connector.setReferralHandling("always");

        try {
            connector.validateInputParameters();
            fail("Should fail");
        } catch (final ConnectorValidationException e) {
            assertThat(e).hasMessageContaining("referralHandling must be either ignore or follow!");
        }

        connector = getBasicSettings();
        connector.setReferralHandling("IGNORE");

        try {
            connector.validateInputParameters();
            fail("Should fail");
        } catch (final ConnectorValidationException e) {
            assertThat(e).hasMessageContaining("referralHandling must be either ignore or follow!");
        }

        connector = getBasicSettings();
        connector.setReferralHandling("FOLLOW");
        try {
            connector.validateInputParameters();
            fail("Should fail");
        } catch (final ConnectorValidationException e) {
            assertThat(e).hasMessageContaining("referralHandling must be either ignore or follow!");
        }
    }

    @Test
    void testSetDereferencingAliasFromString() throws Exception {
        LdapConnector connector = getBasicSettings();
        connector.setDerefAliases("never");
        assertEquals(LdapDereferencingAlias.NEVER, connector.getDerefAliases());
    }

    @Test
    void testSetReferralHandling() throws BonitaException {
        LdapConnector connector = getBasicSettings();
        connector.setReferralHandling("ignore");
        connector.validateInputParameters();
        connector.setReferralHandling("follow");
        connector.validateInputParameters();
    }
}
