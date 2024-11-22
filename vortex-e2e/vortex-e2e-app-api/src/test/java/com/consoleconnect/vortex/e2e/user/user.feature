Feature: User API

  Background:
    * url apiBaseUrl

  Scenario: Retrieve the login user's details without token
    Given path '/auth/token'
    When method get
    Then status 401

  Scenario: Retrieve the login user's details with valid token, check status and response schema
    * path '/auth/token'
    * configure headers = { 'Authorization': '#(resellerAdminToken)' }
    * method get
    * status 200
    * def schema = read('classpath:schemas/user-schema.json')
    * match response.data contains deep schema
