import { connectionRequestBody, connectionResponse, organizationResponse } from '../mockData'

describe('Mock Data Unit Tests', () => {
  describe('organizationResponse', () => {
    it('should have a valid structure for CreateOrganizationResponse', () => {
      expect(organizationResponse).toHaveProperty('code', 200)
      expect(organizationResponse).toHaveProperty('message', 'OK')
      expect(organizationResponse).toHaveProperty('data')

      const data = organizationResponse.data
      expect(data).toHaveProperty('id', '')
      expect(data).toHaveProperty('name', '')
      expect(data).toHaveProperty('display_name', '')
      expect(data).toHaveProperty('metadata')
      expect(data.metadata).toEqual({
        status: '',
        connectionId: '',
        strategy: ''
      })

      expect(data).toHaveProperty('branding')
      expect(data.branding).toHaveProperty('colors')
      expect(data.branding.colors).toEqual({
        primary: '',
        page_background: ''
      })
      expect(data.branding).toHaveProperty('logo_url', '')

      expect(data).toHaveProperty('connection')
      expect(data.connection).toEqual({
        display_name: '',
        enabled_clients: [],
        id: '',
        metadata: {
          additionalProp1: '',
          additionalProp2: '',
          additionalProp3: ''
        },
        name: '',
        options: {
          additionalProp1: {},
          additionalProp2: {},
          additionalProp3: {}
        },
        provisioning_ticket_url: '',
        realms: '',
        strategy: ''
      })
    })
  })

  describe('connectionResponse', () => {
    it('should have a valid structure for RequestResponse<AddConnectionResponse>', () => {
      expect(connectionResponse).toHaveProperty('code', 200)
      expect(connectionResponse).toHaveProperty('message', 'OK')
      expect(connectionResponse).toHaveProperty('data')

      const data = connectionResponse.data
      expect(data).toHaveProperty('name', 'riejantest-samlp-oBzndE')
      expect(data).toHaveProperty('strategy', 'samlp')
      expect(data).toHaveProperty('options')
      expect(data.options).toEqual({
        signInEndpoint: '',
        signingCert: '',
        debug: true,
        signOutEndpoint: '',
        signSAMLRequest: false,
        digestAlgorithm: '',
        signatureAlgorithm: '',
        fieldsMap: {},
        expires: new Date('2036-12-25T22:32:54.000Z'),
        subject: {
          commonName: ''
        },
        thumbprints: [''],
        cert: ''
      })

      expect(data).toHaveProperty('id', '')
      expect(data).toHaveProperty('enabled_clients', [''])
      expect(data).toHaveProperty('provisioning_ticket_url', '')
      expect(data).toHaveProperty('realms', [''])
    })
  })

  describe('connectionRequestBody', () => {
    it('should have a valid structure for AddConnectionRequestBody', () => {
      expect(connectionRequestBody).toHaveProperty('strategy', 'samlp')
      expect(connectionRequestBody).toHaveProperty('saml')

      const saml = connectionRequestBody.saml
      expect(saml).toEqual({
        signingCert: '',
        signSAMLRequest: true,
        signatureAlgorithm: '',
        digestAlgorithm: '',
        fieldsMap: {},
        signInEndpoint: '',
        signOutEndpoint: '',
        debug: true
      })
    })
  })
})
