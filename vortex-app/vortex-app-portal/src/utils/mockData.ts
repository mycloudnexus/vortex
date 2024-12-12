import { CreateOrganizationResponse } from '@/services/types'

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
