Feature: Invitation API

  Background:
    * url apiBaseUrl
    * configure headers = { 'Authorization': '#(resellerAdminToken)' }
    * def randomWord = function(length) { var result = ''; karate.repeat(length, function(){ result += String.fromCharCode(Math.floor(Math.random() * 26) + 97); }); return result; }

  @P0
  @ignore
  @known-issue
  Scenario: Invite a user into customer company, then delete the invitation
    * def data = read('classpath:data/organization/create_invitation.json')
    * set data.email = randomWord(10) + '+' + randomWord(6) + '@wltest.com'
    * set data.roles = ["ORG_MEMBER"]

    * path 'mgmt/organizations', customerAOrgId, 'invitations'
    * request data
    * method post
    * status 200
    * def invitationId = response.data.id
    * def schema = read('classpath:schemas/invitation-schema.json')
    * match response.data == schema
    * def invitation = response.data

    * path 'mgmt/organizations', customerAOrgId, 'invitations', invitationId
    * method get
    * status 200
    * match response.data == invitation

    * path 'mgmt/organizations', customerAOrgId, 'invitations', invitationId
    * method delete
    * status 200

