Feature: Invitation API

  Background:
    * url apiBaseUrl
    * configure headers = { 'Authorization': '#(resellerAdminToken)' }
    * def randomWord = function(length) { var result = ''; karate.repeat(length, function(){ result += String.fromCharCode(Math.floor(Math.random() * 26) + 97); }); return result; }

  @P0
  Scenario: Invite a user into reseller, then delete the invitation
    * def data = read('classpath:data/create_invitation.json')
    * set data.email = randomWord(10) + '+' + randomWord(6) + '@wltest.com'
    * set data.roles = ["PLATFORM_MEMBER"]

    * path 'mgmt/organizations', resellerOrgId, 'invitations'
    * request data
    * method post
    * status 200
    * def invitationId = response.data.id
    * def schema = read('classpath:schemas/invitation-schema.json')
    * match response.data == schema
    * def invitation = response.data

    * path 'mgmt/organizations', resellerOrgId, 'invitations', invitationId
    * method get
    * status 200
    * match response.data == invitation

    * path 'mgmt/organizations', resellerOrgId, 'invitations', invitationId
    * method delete
    * status 200

