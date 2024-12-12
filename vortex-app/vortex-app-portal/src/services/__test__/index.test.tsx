import request from '@/utils/helpers/request'
import { createConnection, createOrganization, getCompanyList, getOrganizationById, updateOrganization } from '..'
import type {
  AddConnectionRequestBody,
  AddConnectionResponse,
  CreateOrganizationRequestBody,
  CreateOrganizationResponse,
  IOrganization,
  RequestResponse,
  UpdateOrganizationRequestBody
} from '../types'

jest.mock('@/utils/helpers/request')

describe('Api calls', () => {
  const ORGANIZATIONS = '/mgmt/organizations'

  describe('update organization', () => {
    let mockPatch = jest.fn()
    beforeEach(() => {
      jest.clearAllMocks()
      mockPatch = request.patch as jest.Mock
    })
    it('should successfully update an organization and return the response data', async () => {
      const mockResponseData: CreateOrganizationResponse = {
        code: 200,
        message: 'OK',
        data: {
          id: '123',
          name: 'Updated Org',
          display_name: 'Updated Org',
          metadata: {
            status: 'ACTIVE',
            connectionId: 'CUSTOMER',
            strategy: 'undefined'
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
      mockPatch.mockResolvedValueOnce({ data: mockResponseData })

      const req: UpdateOrganizationRequestBody = {
        id: '123',
        request_body: { display_name: 'Updated Org' }
      }

      const result = await updateOrganization(req)

      expect(mockPatch).toHaveBeenCalledWith(`${ORGANIZATIONS}/${req.id}`, req.request_body)
      expect(result).toEqual(mockResponseData)
    })

    it('should throw an error if the API call fails', async () => {
      const mockError = new Error('Network Error')
      mockPatch.mockRejectedValueOnce(mockError)
      const req: UpdateOrganizationRequestBody = {
        id: '123',
        request_body: { display_name: 'Failing Org' }
      }
      await expect(updateOrganization(req)).rejects.toThrow(mockError)
      expect(mockPatch).toHaveBeenCalledWith(`${ORGANIZATIONS}/${req.id}`, req.request_body)
    })
  })

  describe('create organization', () => {
    let mockAdd = jest.fn()
    beforeEach(() => {
      jest.clearAllMocks()
      mockAdd = request.post as jest.Mock
    })

    it('should create a organization', async () => {
      const mockResponse: CreateOrganizationResponse = {
        code: 200,
        message: 'OK',
        data: {
          id: '123',
          name: 'adding1',
          display_name: 'adding1',
          metadata: {
            status: 'ACTIVE',
            connectionId: 'CUSTOMER',
            strategy: 'undefined'
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
      mockAdd.mockResolvedValueOnce({ data: mockResponse })
      const req: CreateOrganizationRequestBody = {
        display_name: 'adding1',
        name: 'adding1'
      }
      const result = await createOrganization(req)

      expect(mockAdd).toHaveBeenCalledWith(ORGANIZATIONS, req)
      expect(result).toEqual(mockResponse)
    })

    it('should throw an error when creating a organization', async () => {
      const mockError = new Error('Network Error')
      mockAdd.mockRejectedValueOnce(mockError)
      const req: CreateOrganizationRequestBody = {
        display_name: 'adding1',
        name: 'adding1'
      }
      await expect(createOrganization(req)).rejects.toThrow(mockError)
      expect(mockAdd).toHaveBeenCalledWith(ORGANIZATIONS, req)
    })
  })

  describe('get organization list', () => {
    let mockGet = jest.fn()
    beforeEach(() => {
      jest.clearAllMocks()
      mockGet = request as unknown as jest.Mock
    })

    it('should get all the list of organization', async () => {
      const mockResponse: RequestResponse<IOrganization> = {
        code: 200,
        message: 'OK',
        data: {
          data: [],
          page: 0,
          size: 200,
          total: 0
        }
      }
      mockGet.mockResolvedValueOnce({ data: mockResponse })
      const result = await getCompanyList()

      expect(mockGet).toHaveBeenCalledWith(ORGANIZATIONS, {
        params: {
          size: -1
        }
      })
      expect(result).toEqual(mockResponse)
    })

    it('should throw an error', async () => {
      const mockError = new Error('Network Error')
      mockGet.mockRejectedValueOnce(mockError)
      await expect(getCompanyList()).rejects.toThrow(mockError)
      expect(mockGet).toHaveBeenCalledWith(ORGANIZATIONS, { params: { size: -1 } })
    })
  })

  describe('get organization by id', () => {
    let mockGetById = jest.fn()
    beforeEach(() => {
      jest.clearAllMocks()
      mockGetById = request as unknown as jest.Mock
    })
    it('should get a organization data', async () => {
      const mockResponse: CreateOrganizationResponse = {
        code: 200,
        message: 'OK',
        data: {
          name: '',
          id: 'org_DhF9POe3xRfNvXtO',
          display_name: '',
          metadata: {
            strategy: '',
            status: '',
            connectionId: ''
          },
          branding: {
            colors: {
              page_background: '',
              primary: ''
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
      mockGetById.mockResolvedValueOnce({ data: mockResponse })
      const result = await getOrganizationById('org_DhF9POe3xRfNvXtO')

      expect(mockGetById).toHaveBeenCalledWith(`${ORGANIZATIONS}/org_DhF9POe3xRfNvXtO`)
      expect(result).toEqual(mockResponse)
    })

    it('should throw an error', async () => {
      const mockError = new Error('Network Error')
      mockGetById.mockRejectedValueOnce(mockError)
      await expect(getOrganizationById('org_DhF9POe3xRfNvXtO')).rejects.toThrow(mockError)
      expect(mockGetById).toHaveBeenCalledWith(`${ORGANIZATIONS}/org_DhF9POe3xRfNvXtO`)
    })
  })

  describe('add connection', () => {
    let mockAddConnection = jest.fn()
    beforeEach(() => {
      jest.clearAllMocks()
      mockAddConnection = request.post as jest.Mock
    })

    it('should create connection', async () => {
      const connectionResponse: RequestResponse<AddConnectionResponse> = {
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

      const connectionRequestBody: AddConnectionRequestBody = {
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
      mockAddConnection.mockResolvedValueOnce({ data: connectionResponse })

      const result = await createConnection('org_JqMlLhQYpEwDO68Z', connectionRequestBody)

      expect(mockAddConnection).toHaveBeenCalledWith(
        `${ORGANIZATIONS}/org_JqMlLhQYpEwDO68Z/connection`,
        connectionRequestBody
      )
      expect(result).toEqual(connectionResponse)
    })

    it('should throw an error when creating a connection', async () => {
      const mockError = new Error('Network Error')
      const connectionRequestBody: AddConnectionRequestBody = {
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
      mockAddConnection.mockRejectedValueOnce(mockError)

      await expect(createConnection('org_JqMlLhQYpEwDO68Z', connectionRequestBody)).rejects.toThrow(mockError)
      expect(mockAddConnection).toHaveBeenCalledWith(
        `${ORGANIZATIONS}/org_JqMlLhQYpEwDO68Z/connection`,
        connectionRequestBody
      )
    })
  })
})
