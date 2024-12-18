Feature: Mgmt Connection API

  Background:
    * url apiBaseUrl
    * configure headers = { 'Authorization': '#(resellerAdminToken)' }

  @P0
  Scenario Outline: Create SAML connection for customer, <case>, then update it, check status and response
    * def update_data = <update_data>
    * call read('classpath:com/consoleconnect/vortex/e2e/organization/mgmt-organization.feature@create-one-organization')
    * path 'mgmt/organizations', organizationId, 'connection'
    * method get
    * status 200

    * call read('@create-connection-for-organization')
    * match response.data contains deep <match_data>

    Examples:
      | case               | update_data                                            | match_data                                                                 |
      | username&password  | {"strategy": "auth0"}                                  | {"connection": {"strategy": "auth0"}}                                      |
      | saml - no mapping  | {"strategy": "samlp"}                                  | {"connection": {"strategy": "samlp"}}                                      |
      | saml - has mapping | {"strategy": "samlp","saml": {"fieldsMap": {"a":"b"}}} | {"connection": {"strategy": "samlp", "options": {"fieldsMap": {"a":"b"}}}} |

  @ignore
  @create-connection-for-organization-ignore-status
  Scenario: Create SAML connection
    * def update_data = karate.get('update_data') || {}
    * def signingCert = karate.exec(['node', 'src/main/java/com/consoleconnect/vortext/toolkit/generate-cert.js'])
    * def data = read('classpath:data/organization/create_connection_saml.json')
    * set data.saml.debug = true
    * def deepMerge = read('classpath:com/consoleconnect/vortext/toolkit/deep-merge.js')
    * def payload = deepMerge(data, update_data)
    * path 'mgmt/organizations', organizationId, 'connection'
    * request payload
    * method post

  @ignore
  @create-connection-for-organization
  Scenario: Create SAML connection
    * call read('@create-connection-for-organization-ignore-status')
    * match responseStatus == 200
    * def connectionId = response.data.connection_id
    * def connection = response.data.connection

  @ignore
  @create-one-organization-with-connection
  Scenario: Create an organization with SAML connection
    * call read('classpath:com/consoleconnect/vortex/e2e/organization/mgmt-organization.feature@create-one-organization')
    * call read('@create-connection-for-organization')

  @ignore
  @get-organization-connection
  Scenario: Get reseller organization connection, check status and response
    * path 'mgmt/organizations', organizationId, 'connection'
    * method get
    * status 200
