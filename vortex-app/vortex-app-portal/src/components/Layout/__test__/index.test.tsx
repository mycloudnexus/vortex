import { render, screen, act } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from 'react-query'
import Layout from '..'
import * as Auth0 from '@auth0/auth0-react'

jest.mock('@/utils/helpers/request')
const mockedUsedNavigate = jest.fn()
const queryClient = new QueryClient()
jest.mock('@/stores/app.store', () => ({
  useAppStore: () => {
    return {
      userType: 'reseller',
      currentAuth0User: {}
    }
  }
}))
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockedUsedNavigate
}))

const BaseLayout = () => (
  <QueryClientProvider client={queryClient}>
    <Layout />
  </QueryClientProvider>
)

describe('Layout component', () => {
  it('renders the Authenticate component with authenticated ', async () => {
    jest
      .spyOn(Auth0, 'useAuth0')
      .mockReturnValue({ isAuthenticated: true, isLoading: false, getAccessTokenSilently: jest.fn() } as any)

    const { container } = await act(async () => render(<BaseLayout />))
    expect(container.firstChild).toMatchSnapshot()
  })
})
