import { CreateOrganizationResponse, IOrganization, RequestResponse } from '@/services/types'
import { renderHook, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from 'react-query'
import { useAddOrganization, useGetCompanyList, useGetOrganizationById, useUpdateOrganization } from '../company'
import { createOrganization, getCompanyList, getOrganizationById, updateOrganization } from '@/services'

jest.mock('@/services', () => ({
  getCompanyList: jest.fn(),
  createOrganization: jest.fn(),
  updateOrganization: jest.fn(),
  getOrganizationById: jest.fn()
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
      code: 200,
      message: 'OK',
      data: {
        id: '123',
        name: 'addorg',
        display_name: 'add org',
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
      code: 200,
      message: 'OK',
      data: {
        id: '123',
        name: 'add org',
        display_name: 'update',
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
      code: 200,
      message: 'OK',
      data: {
        name: '',
        id: 'org_DhF9POe3xRfNvXtO',
        display_name: '',
        metadata: {
          loginType: '',
          status: '',
          type: ''
        },
        branding: {
          colors: {
            page_background: '',
            primary: ''
          },
          logo_url: ''
        }
      }
    }
    ;(getOrganizationById as jest.Mock).mockResolvedValue(mockResponse)
    const { result } = renderHook(() => useGetOrganizationById('org_DhF9POe3xRfNvXtO'), { wrapper })
    expect(result.current.isLoading).toBe(true)
    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data).toEqual(mockResponse)
    expect(getOrganizationById).toHaveBeenCalledTimes(1)
  })
})
