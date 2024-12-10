import { QueryClient, QueryClientProvider } from 'react-query'
import { renderHook, waitFor } from '@testing-library/react'
import { useGetUserAuthDetail, useGetUserRole, useGetUserDetail, useGetVortexUser } from '../user'
import { getUserDetail, getUserAuthToken, getUserRole, getVortexUser } from '@/services/user'

jest.mock('@/services/user')

//defining React Query Wrapper
const queryClient = new QueryClient()

const wrapper = ({ children }: { children: React.ReactNode }) => (
  <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
)

const mockUserDetail = {
  name: 'etst',
  email: 'tesrt.com',
  id: '123'
}
const mockRoles = [
  {
    name: 'TESTVIEW',
    id: '123'
  }
]
const mockVortexuser = {
  data: {
    name: 'test',
    eamil: '123@test.com',
    id: '7h0k'
  }
}

describe('useCarEventDetails', () => {
  // beforeEach test BeforeEach function will run and clear the mock and UseQuery
  beforeEach(() => {
    jest.clearAllMocks()
    queryClient.clear()
  })

  it('useGetUserDetail test', async () => {
    ;(getUserDetail as jest.Mock).mockResolvedValue(mockUserDetail)
    const { result } = renderHook(() => useGetUserDetail('test'), {
      wrapper
    })
    await waitFor(() => expect(result.current.isLoading).toBe(false))
    expect(result.current.isError).toBe(false)
    expect(result.current.data).toEqual(mockUserDetail)
  })
  it('useGetUserAuthDetail test', async () => {
    ;(getUserAuthToken as jest.Mock).mockResolvedValue(mockUserDetail)
    const { result } = renderHook(() => useGetUserAuthDetail(), {
      wrapper
    })
    await waitFor(() => expect(result.current.isLoading).toBe(false))
    expect(result.current.isError).toBe(false)
    expect(result.current.data).toEqual(mockUserDetail)
  })
  it('useGetUserRole test', async () => {
    ;(getUserRole as jest.Mock).mockResolvedValue(mockRoles)
    const { result } = renderHook(() => useGetUserRole(), {
      wrapper
    })
    await waitFor(() => expect(result.current.isLoading).toBe(false))
    expect(result.current.isError).toBe(false)
    expect(result.current.data).toEqual(mockRoles)
  })
  it('useGetVortexUser test', async () => {
    ;(getVortexUser as jest.Mock).mockResolvedValue(mockVortexuser)
    const { result } = renderHook(() => useGetVortexUser(), {
      wrapper
    })
    await waitFor(() => expect(result.current.isLoading).toBe(false))
    expect(result.current.isError).toBe(false)
    expect(result.current.data).toEqual(mockVortexuser)
  })
})
