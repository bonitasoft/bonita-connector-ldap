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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;

import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This connector provides an LDAP service of querying directory service. This connector does the search operation.
 *
 * @author Matthieu Chaffotte
 */
public class LdapConnector extends AbstractConnector {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(LdapConnector.class);

    // Input
    public static final String HOST_PARAMETER = "host";
    public static final String PORT_PARAMETER = "port";
    public static final String PROTOCOL_PARAMETER = "protocol";
    public static final String USERNAME_PARAMETER = "username";
    public static final String PASSWORD_PARAMETER = "password";
    public static final String BASE_OBJECT_PARAMETER = "baseObject";
    public static final String SCOPE_PARAMETER = "scope";
    public static final String FILTER_PARAMETER = "filter";
    public static final String ATTRIBUTES_PARAMETER = "attributes";
    public static final String SIZE_LIMIT_PARAMETER = "sizeLimit";
    public static final String PAGE_SIZE_PARAMETER = "pageSize";
    public static final String TIME_LIMIT_PARAMETER = "timeLimit";
    public static final String REFERRAL_HANDLING_PARAMETER = "referralHandling";
    public static final String DEREF_ALIASES_PARAMETER = "derefAliases";

    // Output
    public static final String LDAP_ATTRIBUTE_LIST_OUTPUT = "ldapAttributeList";

    /**
     * The host name of the directory service.
     */
    private String host;

    /**
     * The port number of the directory service.
     */
    private int port = 389;

    /**
     * The protocol used by the directory service. It can be LDAP, LDAPS(SSL) or TLS.
     */
    private LdapProtocol protocol;

    /**
     * The user name if authentication is needed.
     */
    private String userName;

    /**
     * The password if authentication is needed.
     */
    private String password;
    private String certificatePath;
    private String baseObject;
    private LdapScope scope = LdapScope.BASE;
    private String filter;
    private LdapDereferencingAlias derefAliases = LdapDereferencingAlias.ALWAYS;
    private String[] attributes;
    private Long sizeLimit = 0l;
    private Long pageSize = 0l;
    private Integer timeLimit = 0;
    private String referralHandling = "ignore";

    // output
    private List<List<LdapAttribute>> result = new ArrayList<>();

    private String getHost() {
        return host;
    }

    private Integer getPort() {
        return port;
    }

    public LdapProtocol getProtocol() {
        return protocol;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getCertificatePath() {
        return certificatePath;
    }

    public String getBaseObject() {
        return baseObject;
    }

    public LdapScope getScope() {
        return scope;
    }

    public String getFilter() {
        return filter;
    }

    public LdapDereferencingAlias getDerefAliases() {
        return derefAliases;
    }

    public String[] getAttributes() {
        return attributes;
    }

    public long getSizeLimit() {
        return sizeLimit;
    }

    public long getPageSize() {
        return pageSize;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public List<List<LdapAttribute>> getLdapAttributeList() {
        return result;
    }

    public String getReferralHandling() {
        return referralHandling;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public void setPort(final Long port) {
        if (port == null) {
            setPort(Integer.MIN_VALUE);
        } else {
            setPort(port.intValue());
        }
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public void setProtocol(final LdapProtocol protocol) {
        this.protocol = protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = LdapProtocol.LDAPS;
        if (protocol != null) {
            protocol = protocol.toUpperCase();
            if (protocol.equals(LdapProtocol.TLS.toString())) {
                this.protocol = LdapProtocol.TLS;
            } else if (protocol.equals(LdapProtocol.LDAP.toString())) {
                this.protocol = LdapProtocol.LDAP;
            }
        }
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public void setBaseObject(final String baseObject) {
        this.baseObject = baseObject;
    }

    public void setScope(final LdapScope scope) {
        this.scope = scope;
    }

    public void setScope(String scope) {
        this.scope = LdapScope.ONELEVEL;
        if (scope != null) {
            scope = scope.toUpperCase();
            if (scope.equals(LdapScope.BASE.toString())) {
                this.scope = LdapScope.BASE;
            } else if (scope.equals(LdapScope.SUBTREE.toString())) {
                this.scope = LdapScope.SUBTREE;
            }
        }
    }

    public void setFilter(final String filter) {
        this.filter = filter;
    }

    public void setDerefAliases(final LdapDereferencingAlias derefAliases) {
        this.derefAliases = derefAliases;
    }

    public void setDerefAliases(final String derefAliases) {
        if (derefAliases != null && !derefAliases.isEmpty()) {
            this.derefAliases = LdapDereferencingAlias.valueOf(derefAliases.toUpperCase());
        }
    }

    public void setAttributes(final String[] attributes) {
        if (attributes == null) {
            this.attributes = null;
        } else {
            this.attributes = new String[attributes.length];
            System.arraycopy(attributes, 0, this.attributes, 0, attributes.length);
        }
    }

    public void setAttributes(final String attributes) {
        if (attributes == null || "".equals(attributes.trim())) {
            this.attributes = null;
        } else {
            this.attributes = Stream.of(attributes.split(","))
                    .map(String::trim)
                    .toArray(String[]::new);
        }
    }

    public void setSizeLimit(final long sizeLimit) {
        this.sizeLimit = sizeLimit;
    }

    public void setSizeLimit(final Long sizeLimit) {
        if (sizeLimit == null) {
            setSizeLimit(Long.MIN_VALUE);
        } else {
            setSizeLimit(sizeLimit.longValue());
        }
    }

    public void setPageSize(final long pageSize) {
        this.pageSize = pageSize;
    }

    public void setPageSize(final Long pageSize) {
        if (pageSize == null) {
            setPageSize(Long.MIN_VALUE);
        } else {
            setPageSize(pageSize.longValue());
        }
    }

    /**
     * Sets the time-limit during a search in seconds.
     * @param timeLimit, the search time limit in seconds
     */
    public void setTimeLimit(final int timeLimit) {
        this.timeLimit = timeLimit;
    }

    /**
     * Sets the time-limit during a search in seconds.
     * @param timeLimit, the search time limit in seconds
     */
    public void setTimeLimit(final Long timeLimit) {
        if (timeLimit == null) {
            setTimeLimit(Integer.MIN_VALUE);
        } else {
            setTimeLimit(timeLimit.intValue());
        }
    }

    public void setReferralHandling(final String referralHandling) {
        this.referralHandling = referralHandling;
    }

    @Override
    public void setInputParameters(Map<String, Object> parameters) {
        setHost((String) parameters.get(HOST_PARAMETER));
        setPort((Integer) parameters.get(PORT_PARAMETER));
        setProtocol((String) parameters.get(PROTOCOL_PARAMETER));
        setUserName((String) parameters.get(USERNAME_PARAMETER));
        setPassword((String) parameters.get(PASSWORD_PARAMETER));
        setBaseObject((String) parameters.get(BASE_OBJECT_PARAMETER));
        setScope((String) parameters.get(SCOPE_PARAMETER));
        setFilter((String) parameters.get(FILTER_PARAMETER));
        setAttributes((String) parameters.get(ATTRIBUTES_PARAMETER));
        setSizeLimit((Long) parameters.get(SIZE_LIMIT_PARAMETER));
        setPageSize((Long) parameters.get(PAGE_SIZE_PARAMETER));
        setTimeLimit((Long) parameters.get(TIME_LIMIT_PARAMETER));
        setReferralHandling((String) parameters.get(REFERRAL_HANDLING_PARAMETER));
        setDerefAliases((String) parameters.get(DEREF_ALIASES_PARAMETER));
    }

    private Hashtable<String, String> getEnvironment() {
        final Hashtable<String, String> environment = new Hashtable<>();
        environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        environment.put(Context.PROVIDER_URL, "ldap://" + getHost() + ":" + getPort());
        if (getProtocol().equals(LdapProtocol.LDAPS)) {
            environment.put(Context.SECURITY_PROTOCOL, "ssl");
        }
        if (!LdapProtocol.TLS.equals(getProtocol()) && getUserName() != null && getPassword() != null) {
            environment.put(Context.SECURITY_AUTHENTICATION, "simple");
            environment.put(Context.SECURITY_PRINCIPAL, getUserName());
            environment.put(Context.SECURITY_CREDENTIALS, getPassword());
        } else {
            environment.put(Context.SECURITY_AUTHENTICATION, "none");
        }
        environment.put("java.naming.ldap.derefAliases", getDerefAliases().toString().toLowerCase());
        environment.put(Context.REFERRAL, getReferralHandling());
        return environment;
    }

    @Override
    protected void executeBusinessLogic() throws ConnectorException {
        final Hashtable<String, String> env = getEnvironment();
        final LdapContext ctx;
        try {
            ctx = new InitialLdapContext(env, null);
        } catch (final NamingException e) {
            throw new ConnectorException(e);
        }

        StartTlsResponse response = null;
        try {
            if (LdapProtocol.TLS.equals(getProtocol())) {
                final StartTlsRequest request = new StartTlsRequest();
                response = (StartTlsResponse) ctx.extendedOperation(request);
                response.negotiate();
                if (getUserName() != null && getPassword() != null) {
                    ctx.addToEnvironment(Context.SECURITY_AUTHENTICATION, "simple");
                    ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, getUserName());
                    ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, getPassword());
                }
            }
            final SearchControls ctls = new SearchControls();
            ctls.setTimeLimit(getTimeLimit() * 1000);
            ctls.setCountLimit(getSizeLimit());
            ctls.setReturningAttributes(getAttributes());
            ctls.setSearchScope(getScope().value());
            if (getPageSize() > 0) {
                doPagedSearch(ctx, ctls);
            } else {
                doNonPagedSearch(ctx, ctls);
            }
            setOutputParameter(LDAP_ATTRIBUTE_LIST_OUTPUT, result);
        } catch(IOException | NamingException e) {
            throw new ConnectorException(e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (final IOException e) {
                    LOGGER.error("Error closing the StartTlsResponse", e);
                }
            }

            try {
                ctx.close();
            } catch (final NamingException e) {
                LOGGER.error("Error closing the LDAP context", e);
            }
        }

    }

    private void addSearchResult(SearchResult sr) throws NamingException {
        final Attributes attribs = sr.getAttributes();
        final NamingEnumeration<? extends Attribute> enume = attribs.getAll();
        final List<LdapAttribute> elements = new ArrayList<>();
        while (enume.hasMore()) {
            final Attribute attribute = enume.next();
            final NamingEnumeration<?> all = attribute.getAll();
            while (all.hasMore()) {
                final Object key = all.next();
                String value;
                if (key instanceof byte[]) {
                    value = new String((byte[]) key, StandardCharsets.UTF_8);
                } else {
                    value = key.toString();
                }
                elements.add(new LdapAttribute(attribute.getID(), value));
            }
        }
        if (!elements.isEmpty()) {
            result.add(elements);
        }
    }

    private void doNonPagedSearch(LdapContext ctx, SearchControls ctls) throws NamingException {
        final NamingEnumeration<SearchResult> answer = ctx.search(getBaseObject(), getFilter(), ctls);
        long count = getSizeLimit();
        // count is useful in case of the size-limit is defined
        // the search method does not care about size-limit. It returns all entries
        // which match with the filter.
        if (count == 0) {
            count = Long.MAX_VALUE;
        }
        result = new ArrayList<>();
        while (count > 0 && answer.hasMore()) {
            final SearchResult sr = answer.next();
            count--;
            addSearchResult(sr);
        }
    }

    private void doPagedSearch(LdapContext ctx, SearchControls ctls) throws NamingException, IOException {
        result = new ArrayList<>();
        byte[] cookie = null;
        ctx.setRequestControls(new Control[] {new PagedResultsControl((int) getPageSize(), Control.NONCRITICAL)});
        do {
            NamingEnumeration<SearchResult> answer = ctx.search(getBaseObject(), getFilter(), ctls);
            while (answer.hasMoreElements()) {
                final SearchResult sr = answer.next();
                addSearchResult(sr);
            }
            Control[] controls = ctx.getResponseControls();
            if (controls != null) {
                for (Control control : controls) {
                    if (control instanceof PagedResultsResponseControl) {
                        PagedResultsResponseControl prrc = (PagedResultsResponseControl) control;
                        cookie = prrc.getCookie();
                    }
                }
            }
            ctx.setRequestControls(new Control[] { new PagedResultsControl((int) getPageSize(), cookie, Control.CRITICAL) });
        } while (cookie != null);
    }

    @Override
    public void validateInputParameters() throws ConnectorValidationException {
        final List<String> errors = new ArrayList<>();
        if (host == null || host.length() == 0) {
            errors.add("host cannot be empty!");
        }

        if (userName == null || userName.length() == 0) {
            if (password != null && password.length() != 0) {
                errors.add("username cannot be empty!");
            }
        } else {
            if (password == null || password.length() == 0) {
                errors.add("password cannot be empty!");
            }
        }

        if (baseObject == null || baseObject.length() == 0) {
            errors.add("baseObject cannot be empty!");
        }

        if (filter == null || filter.length() == 0) {
            errors.add("filter cannot be empty!");
        }

        if (port < 0) {
            errors.add("port cannot be less than 0!");
        } else if (getPort() > 65535) {
            errors.add("port cannot be greater than 65535!");
        }

        if (protocol == null) {
            errors.add("protocol cannot be null");
        } else {
            switch (getProtocol()) {
                case LDAP:
                case LDAPS:
                case TLS:
                    break;
                default:
                    errors.add("Unknown protocol");
                    break;
            }
        }

        if (scope == null) {
            errors.add("scope cannot be null");
        } else {
            switch (getScope()) {
                case BASE:
                case ONELEVEL:
                case SUBTREE:
                    break;
                default:
                    errors.add("Unknown scope");
                    break;
            }

        }

        if (getCertificatePath() != null) {
            final File temp = new File(certificatePath);
            if (!temp.exists()) {
                errors.add("Certificate path does not refer to a real file!");
            }
        }

        if (sizeLimit == null || sizeLimit < 0) {
            errors.add("sizeLimit cannot be null or negative");
        }

        if (timeLimit == null || getTimeLimit() < 0) {
            errors.add("timeLimit cannot be null or negative");
        }

        if (getReferralHandling() == null) {
            errors.add("referralHandling is null!");
        } else if (!getReferralHandling().equals("ignore") && !getReferralHandling().equals("follow")) {
            errors.add("referralHandling must be either ignore or follow!");
        }
        final String derefAliasesInput = (String) getInputParameter(DEREF_ALIASES_PARAMETER);
        if (derefAliasesInput != null && !derefAliasesInput.isEmpty()) {
            try {
                LdapDereferencingAlias.valueOf(derefAliasesInput);
            } catch (final IllegalArgumentException e) {
                throw new ConnectorValidationException(
                        String.format("%s is not a valid dereferencing alias.", derefAliasesInput));
            }
        }

        if (!errors.isEmpty()) {
            throw new ConnectorValidationException(this, errors);
        }
    }
}
