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
      | case                                | name    |
      | contains uppercase letter           | "AAAOP"      |
      | first letter is '_'                 | "_"          |
      | first letter is '-'                 | "-"          |
      | contains special characters - space | " 1"         |
      | contains special characters         | "@#$%^&*()+" |


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