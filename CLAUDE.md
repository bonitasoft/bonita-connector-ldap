# Bonita LDAP Connector

## Project Overview

- **Name**: Bonita LDAP Connector
- **Artifact**: `org.bonitasoft.connectors:bonita-connector-ldap`
- **Version**: 1.2.2-SNAPSHOT
- **Description**: Bonita connector that performs LDAP directory search operations. Supports LDAP, LDAPS (SSL), and TLS protocols, paged results, and configurable scopes/filters.
- **License**: GPL-2.0
- **Tech stack**: Java 11, Maven, Bonita Engine 7.13.0, Java JNDI (`javax.naming`), SLF4J, JUnit 5, AssertJ

## Build Commands

```bash
# Full build with tests (default goal)
./mvnw clean verify

# Skip tests
./mvnw clean verify -DskipTests

# Run tests only
./mvnw test

# Check license headers (runs at validate phase automatically)
./mvnw validate

# Apply/format license headers
./mvnw license:format

# Package connector ZIP
./mvnw clean package

# Deploy to Maven Central (requires GPG key)
./mvnw clean deploy -Pdeploy
```

The build produces:
- `target/bonita-connector-ldap-<version>.jar`
- `target/bonita-connector-ldap-<version>-*.zip` — Bonita connector assembly

## Architecture

### Class hierarchy

```
AbstractConnector (bonita-common)
  └── LdapConnector          # Single connector; all LDAP search logic

LdapAttribute               # Simple value object: attributeId + value (String)
LdapProtocol (enum)         # LDAP | LDAPS | TLS
LdapScope (enum)            # BASE | ONELEVEL | SUBTREE  →  maps to SearchControls constants
LdapDereferencingAlias (enum) # ALWAYS | FINDING | NEVER | SEARCHING
```

### Key patterns

- **Custom `setInputParameters()`**: `LdapConnector` overrides `setInputParameters()` to eagerly parse and type-convert all connector inputs (port, scope, protocol, derefAliases) into strongly-typed fields. This means the connector's fields are ready before `validateInputParameters()` runs.
- **Paged vs. non-paged search**: `executeBusinessLogic()` branches on `pageSize > 0` to call either `doPagedSearch()` (LDAP paged results control) or `doNonPagedSearch()`.
- **TLS upgrade**: When protocol is `TLS`, a `StartTlsRequest` is sent over the initial plain LDAP context before credentials are set.
- **Output**: `ldapAttributeList` — `List<List<LdapAttribute>>`, one inner list per matching DN.
- **License check**: `license-maven-plugin` enforces the GPL header on all `.java` files at `validate`.

## Testing Strategy

- Framework: JUnit 5 (Jupiter) + AssertJ
- `LdapConnectorTest` covers input validation (all error conditions), parameter parsing (enum conversions, port ranges), and the `setInputParameters()` mapping.
- No integration test against a real LDAP server is included; tests are purely unit-level.
- Coverage enforced via JaCoCo.
- SonarCloud project key: `bonitasoft_bonita-connector-ldap`.

## Commit Message Format

Use [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <short description>

[optional body]

[optional footer(s)]
```

Common types: `feat`, `fix`, `chore`, `refactor`, `test`, `docs`, `ci`.

Examples:
```
feat: support paged LDAP search results
fix: validate port range correctly (0-65535)
chore: bump JUnit Jupiter to 5.11.0
```

## Release Process

1. Remove `-SNAPSHOT` from `version` in `pom.xml`.
2. Update `connector-definition-version` if the connector definition changed.
3. Commit: `chore: release X.Y.Z`.
4. Tag: `git tag X.Y.Z`.
5. Deploy to Maven Central: `./mvnw clean deploy -Pdeploy` (requires GPG key and Central credentials in `~/.m2/settings.xml`).
6. Push tag: `git push origin X.Y.Z`.
7. Bump to next `-SNAPSHOT` and commit: `chore: prepare next development iteration`.
