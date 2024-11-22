Feature: Mgmt User API

  Background:
    * url apiBaseUrl
    * configure headers = { 'Authorization': '#(resellerAdminToken)' }
    * def randomWord = function(length) { var result = ''; karate.repeat(length, function(){ result += String.fromCharCode(Math.floor(Math.random() * 26) + 97); }); return result; }
    * configure readTimeout = 100000


  @P0
  Scenario: Get all users under reseller, check status and response schema
    * path '/mgmt/users'
    * method get
    * status 200
    * def schema = read('classpath:schemas/reseller-user-schema.json')
    * match each response.data.data contains deep schema

  @P0
  @ignore
  Scenario: Add a user into reseller, check status
    # TODO ignore as it's not completed
    * def update_data = { userId: '', sendEmail: true}

    * call read('@add-user-into-reseller')
    * assert responseStatus == 200

  @P1
  Scenario: Add a non-existing user into reseller, check status
    * def update_data = { userId: randomWord(10) }

    * call read('@add-user-into-reseller')
    * assert responseStatus == 404
    * match response.reason == 'Member not found'


  @ignore
  @add-user-into-reseller
  Scenario: Add a user into reseller
    * def update_data = karate.get('update_data') || {}
    * def data = read('classpath:data/user/add_user.json')
    * set data.roles = ["PLATFORM_MEMBER"]
    * def deepMerge = read('classpath:com/consoleconnect/vortext/toolkit/deep-merge.js')
    * def payload = deepMerge(data, update_data)

    * path '/mgmt/users'
    * request payload
    * method post

