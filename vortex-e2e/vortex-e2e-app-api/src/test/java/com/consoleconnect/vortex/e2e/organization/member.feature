Feature: Mgmt Member API

  Background:
    * url apiBaseUrl
    * configure headers = { 'Authorization': '#(resellerAdminToken)' }

  @P0
  Scenario: List all members of a customer company, check status and response schema
    * path 'mgmt/organizations', customerAOrgId, 'members'
    * method get
    * status 200
    * def schema = read('classpath:schemas/member-schema.json')
    * match each response.data.data contains deep schema

  @P1
  Scenario: Reseller user reset customer user's password, login type is username&password
    * call read('@get-member-list') { orgId: "#(customerAOrgId)" }
    * def member = members[0]
    * def memberId = member.user_id
    * call read('@reseller-reset-password')
    * assert responseStatus == 200

  @P1
  Scenario: Reseller user reset customer user's password, login type is SSO
    * call read('@get-member-list') { orgId: "#(customerSSOOrgId)" }
    * def member = members[0]
    * def memberId = member.user_id
    * call read('@reseller-reset-password')
    * assert responseStatus == 400

  @ignore
  @get-member-list
  Scenario: Reseller user get company member list
    * path 'mgmt/organizations', orgId, 'members'
    * method get
    * status 200
    * def members = response.data.data

  @ignore
  @reseller-reset-password
  Scenario: Reseller user reset customer user's password
    * path 'mgmt/organizations', orgId, 'members', memberId, 'reset-password'
    * method post
