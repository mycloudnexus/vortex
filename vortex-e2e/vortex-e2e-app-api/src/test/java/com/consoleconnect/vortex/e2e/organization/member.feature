Feature: Mgmt Member API

  Background:
    * url apiBaseUrl
    * configure headers = { 'Authorization': '#(resellerAdminToken)' }

  @P0
  Scenario: List all members of reseller, check status and response schema
    * path 'mgmt/organizations', resellerOrgId, 'members'
    * method get
    * status 200
    * def schema = read('classpath:schemas/user-schema.json')
    * match each response.data.data contains deep schema
