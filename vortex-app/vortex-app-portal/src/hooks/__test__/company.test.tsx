import { CreateOrganizationResponse, IOrganization, RequestResponse } from '@/services/types'
import { renderHook, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from 'react-query'
import {
  useAddOrganization,
  useCreateConnection,
  useGetCompanyList,
  useGetOrganizationById,
  useUpdateOrganization
} from '../company'
import {
  createConnection,
  createOrganization,
  getCompanyList,
  getOrganizationById,
  updateOrganization
} from '@/services'
import { connectionRequestBody, connectionResponse, organizationResponse } from '@/__mocks__/api'

jest.mock('@/services', () => ({
  getCompanyList: jest.fn(),
  createOrganization: jest.fn(),
  updateOrganization: jest.fn(),
  getOrganizationById: jest.fn(),
  createConnection: jest.fn()
}))

describe('Customer hooks', () => {
  let queryClient = new QueryClient()

  const wrapper = ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  )
  afterEach(() => {
    queryClient.clear()
  })

  it('should fetch the company list successfully', async () => {
    const mockResponse: RequestResponse<IOrganization> = {
      code: 200,
      data: {
        data: [],
        page: 1,
        size: 200,
        total: 1
      },
      message: 'OK'
    }
    ;(getCompanyList as jest.Mock).mockResolvedValue(mockResponse)
    const { result } = renderHook(() => useGetCompanyList(), { wrapper })
    expect(result.current.isLoading).toBe(true)
    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data).toEqual(mockResponse)
    expect(getCompanyList).toHaveBeenCalledTimes(1)
  })

  it('should add an organization successfully', async () => {
    const mockRequest = {
      name: 'add org',
      display_name: 'addorg'
    }

    const mockResponse: CreateOrganizationResponse = {
      ...organizationResponse,
      data: {
        ...organizationResponse.data,
        id: '123',
        name: 'addorg',
        display_name: 'add org',
        metadata: {
          status: 'ACTIVE',
          connectionId: 'CUSTOMER',
          strategy: 'undefined'
        }
      }
    }
    ;(createOrganization as jest.Mock).mockResolvedValue(mockResponse)
    const { result } = renderHook(() => useAddOrganization(), { wrapper })

    await waitFor(() =>
      result.current.mutateAsync(mockRequest).then((response) => {
        expect(response).toEqual(mockResponse)
      })
    )

    expect(createOrganization).toHaveBeenCalledTimes(1)
    expect(createOrganization).toHaveBeenCalledWith(mockRequest)
  })

  it('should update an organization successfully', async () => {
    const mockRequest = {
      id: '123',
      request_body: {
        display_name: 'update'
      }
    }

    const mockResponse: CreateOrganizationResponse = {
      ...organizationResponse,
      data: {
        ...organizationResponse.data,
        id: '123',
        name: 'add org',
        display_name: 'update',
        metadata: {
          status: 'ACTIVE',
          connectionId: 'CUSTOMER',
          strategy: 'undefined'
        }
      }
    }
    ;(updateOrganization as jest.Mock).mockResolvedValue(mockResponse)
    const { result } = renderHook(() => useUpdateOrganization(), { wrapper })

    await waitFor(() => {
      result.current.mutateAsync(mockRequest)
    })

    expect(result.current.isSuccess).toBe(true)
    expect(updateOrganization).toHaveBeenCalledTimes(1)
    expect(updateOrganization).toHaveBeenCalledWith(mockRequest)
  })

  it('should fetch org data', async () => {
    const mockResponse: CreateOrganizationResponse = {
      ...organizationResponse,
      data: {
        ...organizationResponse.data,
        id: 'org_DhF9POe3xRfNvXtO'
      }
    }
    ;(getOrganizationById as jest.Mock).mockResolvedValue(mockResponse)
    const { result } = renderHook(() => useGetOrganizationById('org_DhF9POe3xRfNvXtO'), { wrapper })
    expect(result.current.isLoading).toBe(true)
    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data).toEqual(mockResponse)
    expect(getOrganizationById).toHaveBeenCalledTimes(1)
  })

  it('should add a connection successfully', async () => {
    ;(createConnection as jest.Mock).mockResolvedValue(connectionResponse)
    const { result } = renderHook(() => useCreateConnection(), { wrapper })

    await waitFor(() =>
      result.current.mutateAsync({ orgId: '', req: connectionRequestBody }).then((response) => {
        expect(response).toEqual(connectionResponse)
      })
    )

    expect(createConnection).toHaveBeenCalledTimes(1)
    expect(createConnection).toHaveBeenCalledWith('', connectionRequestBody)
  })
})
