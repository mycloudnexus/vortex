Feature: Organization API

  Background:
    * url apiBaseUrl
    * configure headers = { 'Authorization': '#(resellerAdminToken)' }
    * def randomWord = function(length) { var result = ''; karate.repeat(length, function(){ result += String.fromCharCode(Math.floor(Math.random() * 26) + 97); }); return result; }

  @P0
  Scenario: Create an organization with valid payload, check status and response schema
    * def data = read('classpath:data/create_organization.json')
    * def name =  'wl-autotest-' + randomWord(6)
    * def displayName =  name + '-' + 'testing'
    * set data.name = name
    * set data.display_name = displayName

    * path 'mgmt/organizations'
    * request data
    * method post
    * status 200
    * def schema = read('classpath:schemas/organization-schema.json')
    * match response.data == schema
    * match response.data.name == name
    * match response.data.display_name == displayName

  @P2
  Scenario: Create an organization with long name, check validation
    * def longName =  'wl-autotest-21-' + randomWord(6)
    * def name = longName
    * def displayName = 'wl-autotest-' + randomWord(6)

    * call read('@create-one-organization-ignore-status')
    * match responseStatus == 400
    * match response.reason == 'Name cannot exceed 20 characters.'

    * def longName =  'wl-autotest-256-' + randomWord(240)
    * def displayName = longName
    * def name = 'wl-autotest-' + randomWord(6)

    * call read('@create-one-organization-ignore-status')
    * match responseStatus == 400
    * match response.reason == 'Display name cannot exceed 255 characters.'

  @P2
  Scenario Outline: Create an organization with invalid payload, <case>, check status and error message
    * def name = <companyName>
    * def displayName = <displayName>

    * call read('@create-one-organization-ignore-status')
    * match responseStatus == 400
    * match response.reason == "Invalid parameters."

    Examples:
      | case               | companyName   | displayName |
      | empty name         | " "           | "shortName" |
      | empty display name | "companyName" | " "         |

  @P2
  Scenario Outline: Create an organization with invalid name, <case>
    * def name = <name>

    * call read('@create-one-organization-ignore-status')
    * match responseStatus == 400
    * match response.reason == "Invalid name."

    Examples:
      | case                                | name         |
      | contains uppercase letter           | "AAAOP"      |
      | first letter is '_'                 | "_"          |
      | first letter is '-'                 | "-"          |
      | contains special characters - space | " 1"         |
      | contains special characters         | "@#$%^&*()+" |

  @P2
  Scenario: Create an organization with existing name, check validation
    * call read('@get-or-create-one-organization')
    * def existingName = organization.name
    * def existingDisplayName = organization.display_name

    * def newName =  'wl-autotest-' + randomWord(6)
    * def name = existingName
    * def displayName = newName

    * call read('@create-one-organization-ignore-status')
    * match responseStatus == 400
    * match response.reason contains "An organization with this name already exists."

  @P0
  Scenario: Get organization list, check response status and schema
    * path 'mgmt/organizations'
    * method get
    * status 200
    * def schema = read('classpath:schemas/organization-schema.json')
    * match each response.data.data contains deep schema

  @P2
  Scenario: Get organization list, check pagination
    * path 'mgmt/organizations'
    * method get
    * status 200
    * def total = response.data.total
    * assert response.data.page == 0
    * assert response.data.size == 20

    * path 'mgmt/organizations'
    * params { page: 1, size: 1 }
    * method get
    * status 200
    * def difference = response.data.total - total
    * assert difference <= 2 && difference >= -2
    * def result = total <= 1 || karate.match(karate.sizeOf(response.data.data), 1).pass ? { pass: true } : { pass: false }
    * match result == { pass: true }
    * assert response.data.page == 1
    * assert response.data.size == 1

  @P0
  Scenario: Get organization detail, check response status and schema
    * path 'mgmt/organizations'
    * method get
    * status 200
    * def organization = response.data.data[0]
    * path 'mgmt/organizations', organization.id
    * method get
    * status 200
    * def schema = read('classpath:schemas/organization-schema.json')
    * match response.data contains deep schema

  @ignore
  @create-one-organization-ignore-status
  Scenario: Create an organization
    * def data = karate.get('data') || read('classpath:data/create_organization.json')
    * def name =  karate.get('name') || 'wl-autotest-' + randomWord(6)
    * def displayName =  karate.get('displayName') || name + '-' + 'testing'
    * set data.name = name
    * set data.display_name = displayName

    * path 'mgmt/organizations'
    * request data
    * method post

  @ignore
  @create-one-organization
  Scenario: Create an organization
    * def name = 'wl-autotest-' + randomWord(6)
    * def displayName = name + '-' + 'testing'

    * call read('@create-one-organization-ignore-status')
    * match responseStatus == 200
    * def organization = response.data
    * def organizationId = response.data.id

  @ignore
  @get-or-create-one-organization
  Scenario: Get an existing organization, if no organization, create one
    * path 'mgmt/organizations'
    * method get
    * status 200
    * def organizations = karate.filter(response.data.data, function(entry){ return true })
    * if (karate.sizeOf(organizations) > 0 ) karate.set('organization', organizations[0])
    * if (karate.sizeOf(organizations) == 0) karate.call(true, '@create-one-organization')