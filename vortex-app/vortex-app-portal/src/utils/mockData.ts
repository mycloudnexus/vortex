import {
  AddConnectionRequestBody,
  AddConnectionResponse,
  CreateOrganizationResponse,
  RequestResponse
} from '@/services/types'

export const getOrganizationResponse = (): CreateOrganizationResponse => {
  return {
    code: 200,
    message: 'OK',
    data: {
      id: 'org_DhF9POe3xRfNvXtO',
      name: '',
      display_name: '',
      metadata: {
        status: '',
        connectionId: '',
        strategy: ''
      },
      branding: {
        colors: {
          primary: '',
          page_background: ''
        },
        logo_url: ''
      },
      connection: {
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
      }
    }
  }
}

export const getConnectionResponse = (): RequestResponse<AddConnectionResponse> => {
  return {
    code: 200,
    message: 'OK',
    data: {
      name: 'riejantest-samlp-oBzndE',
      strategy: 'samlp',
      options: {
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
      },
      id: '',
      enabled_clients: [''],
      provisioning_ticket_url: '',
      realms: ['']
    }
  }
}

export const getConnectionRequestBody = (): AddConnectionRequestBody => {
  return {
    strategy: 'samlp',
    saml: {
      signingCert: '',
      signSAMLRequest: true,
      signatureAlgorithm: '',
      digestAlgorithm: '',
      fieldsMap: {},
      signInEndpoint: '',
      signOutEndpoint: '',
      debug: true
    }
  }
}
