@parallel=false
Feature: Mgmt Role API

  Background:
    * url apiBaseUrl
    * configure headers = { 'Authorization': '#(resellerAdminToken)' }

  @P0
  Scenario: List all existing roles, check status and response
    * path 'mgmt/roles'
    * method get
    * status 200
    * def expectedRoles = ['PLATFORM_ADMIN', 'PLATFORM_MEMBER']
    * def actualRoles = get response.data
    * match expectedRoles contains actualRoles
    * match actualRoles contains expectedRoles

  @P0
  Scenario: List all roles for an organization
    * path 'mgmt/organizations', resellerOrgId, 'roles'
    * method get
    * status 200
    * assert response.data.total == 2
    # for reseller, there should be 4 roles
    * def expectedRoles = ['ORG_ADMIN', 'ORG_MEMBER']
    * def actualRoles = get response.data.data[*].name
    * match expectedRoles contains actualRoles
    * match actualRoles contains expectedRoles

    * path 'mgmt/organizations'
    * method get
    * status 200
    * def customers = karate.filter(response.data.data, function(entry){ return entry.id != resellerOrgId })

    * path 'mgmt/organizations', customers[0].id, 'roles'
    * method get
    * status 200
    * assert response.data.total == 2
    # for customer, there should be 2 roles
    * def expectedRoles = ['ORG_ADMIN', 'ORG_MEMBER']
    * def actualRoles = get response.data.data[*].name
    * match expectedRoles == actualRoles
    * match actualRoles contains expectedRoles