Feature: User API

  Background:
    * url apiBaseUrl


  Scenario: Retrieve the login user's details
    Given path '/userinfo'
    When method get
    Then status 401

  Scenario: Retrieve the login user's token details
    Given path '/auth/token'
    When method get
    Then status 401
