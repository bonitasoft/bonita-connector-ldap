# LDAP Connector

[![Actions Status](https://github.com/bonitasoft/bonita-connector-ldap/workflows/Build/badge.svg)](https://github.com/bonitasoft/bonita-connector-ldap/actions?query=workflow%3ABuild)
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=bonitasoft_bonita-connector-ldap&metric=alert_status)](https://sonarcloud.io/dashboard?id=bonitasoft_bonita-connector-ldap)
[![GitHub release](https://img.shields.io/github/v/release/bonitasoft/bonita-connector-ldap?color=blue&label=Release)](https://github.com/bonitasoft/bonita-connector-ldap/releases)
[![Maven Central](https://img.shields.io/maven-central/v/org.bonitasoft.connectors/bonita-connector-ldap.svg?label=Maven%20Central&color=orange)](https://search.maven.org/search?q=g:%22org.bonitasoft.connectors%22%20AND%20a:%22bonita-connector-ldap%22)
[![License: GPL v2](https://img.shields.io/badge/License-GPL%20v2-yellow.svg)](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html)

## Build

__Clone__ or __fork__ this repository, then at the root of the project run:

`./mvnw`

## Test

In order to use this connector, you need to have an LDAP server configured.
You can find instructions to deploy an LDAP server with Docker [here](https://docs.digital.ai/bundle/devops-deploy-version-v.22.2/page/deploy/how-to/setup-and-configuration-LDAP-with-deploy.html).<br>
(Adapt `dc=xl,dc=com` and `-e LDAP_ORGANISATION="XL" -e LDAP_DOMAIN="xl.com"` to use `dc=bonita,dc=org` and `-e LDAP_ORGANISATION="bonita" -e LDAP_DOMAIN="bonita.org"` instead if you want to use the ldif file bellow)

To initialize your LDAP server with test data, you can find an example ldif file to import on [bonita-ldap-synchronizer](https://github.com/bonitasoft/bonita-ldap-synchronizer/blob/dev/example/docker/ldap/openldap/resources/01_ldap_data.ldif).

To help you fill the LDAP search wizard in Bonita Studio, you can use the "search" page of phpLDAPadmin which perform similar requests.

### Troubleshooting

If you get an error such as `[LDAP: error code 34 - invalid DN]`<br>
Make sure you use the full username such as `cn=admin,dc=bonita,dc=org`

I you get an exception `java.lang.ClassCastException: class java.util.ArrayList cannot be cast to class java.lang.String (java.util.ArrayList and java.lang.String are in module java.base of loader 'bootstrap')`<br>
Your affected variable must be of type `Java Object` with class `java.util.List`

## Release

In order to create a new release: 
- On the release branch, make sure to update the pom version (remove the -SNAPSHOT)
Run the [release](https://github.com/bonitasoft/bonita-connector-ldap/actions/workflows/release.yml) action, set the version to releae as parameter
- Update the `master` with the next SNAPSHOT version.

Once this is done, update the [Bonita marketplace repository](https://github.com/bonitasoft/bonita-marketplace) with the new version of the connector.

## Contributing

We would love you to contribute, pull requests are welcome! Please see the [CONTRIBUTING.md](CONTRIBUTING.md) for more information.

## License

The sources and documentation in this project are released under the [GPLv2 License](LICENSE)

