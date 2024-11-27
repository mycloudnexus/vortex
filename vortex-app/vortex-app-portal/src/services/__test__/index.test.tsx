import request from '@/utils/helpers/request' // Adjust the import path if using a custom request instance
import { createOrganization, getCompanyList, updateOrganization } from '..'
import type {
  CreateOrganizationRequestBody,
  CreateOrganizationResponse,
  IOrganization,
  RequestResponse,
  UpdateOrganizationRequestBody
} from '../types'

jest.mock('@/utils/helpers/request') // Mocking axios

describe('Api calls', () => {
  const ORGANIZATIONS = '/mgmt/organizations' // Ensure this matches your actual constant

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
            type: 'CUSTOMER',
            loginType: 'undefined'
          },
          branding: {
            colors: {
              primary: '',
              page_background: ''
            },
            logo_url: ''
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
            type: 'CUSTOMER',
            loginType: 'undefined'
          },
          branding: {
            colors: {
              primary: '',
              page_background: ''
            },
            logo_url: ''
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
          size: 200
        }
      })
      expect(result).toEqual(mockResponse)
    })

    it('should throw an error', async () => {
      const mockError = new Error('Network Error')
      mockGet.mockRejectedValueOnce(mockError)
      await expect(getCompanyList()).rejects.toThrow(mockError)
      expect(mockGet).toHaveBeenCalledWith(ORGANIZATIONS, { params: { size: 200 } })
    })
  })
})
