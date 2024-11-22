Feature: Mgmt Connection API

  Background:
    * url apiBaseUrl
    * configure headers = { 'Authorization': '#(resellerAdminToken)' }

  @P0
  Scenario: Get reseller organization connection, check status and response
    * path 'mgmt/organizations', resellerOrgId, 'connections'
    * method get
    * status 200
    * assert response.data.data.length == 1

  @P0
  Scenario Outline: Create SAML connection for customer, <case>, then update it, check status and response
    * def update_data = <update_data>
    * call read('classpath:com/consoleconnect/vortex/e2e/organization/mgmt-organization.feature@create-one-organization')
    * path 'mgmt/organizations', organizationId, 'connections'
    * method get
    * status 200
    * assert response.data.data.length == 0

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
    * def deepMerge =
      """
      function deepMerge(obj1, obj2) {
        var result = JSON.parse(JSON.stringify(obj1));
        for (var key in obj2) {
          if (obj2.hasOwnProperty(key)) {
            if (typeof obj2[key] === 'object' && obj2[key] !== null && typeof result[key] === 'object' && result[key] !== null) {
              result[key] = deepMerge(result[key], obj2[key]);
            } else {
              result[key] = obj2[key];
            }
          }
        }
        return result;
      }
      """
    * def payload = deepMerge(data, update_data)
    * path 'mgmt/organizations', organizationId, 'connections'
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
    * path 'mgmt/organizations', organizationId, 'connections'
    * method get
    * status 200
