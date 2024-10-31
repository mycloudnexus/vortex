import { render, screen } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from 'react-query'

import Authenticate from '../Authenticate'
import { ENV } from '@/constant'
import * as Auth0 from '@auth0/auth0-react'

const mockedUsedNavigate = jest.fn()

const queryClient = new QueryClient()

jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockedUsedNavigate
}))
jest.mock('@auth0/auth0-react')
const Children = <span>childrendom</span>
const AuthenticateDom = () => (
  <QueryClientProvider client={queryClient}>
    <Authenticate>{Children}</Authenticate>
  </QueryClientProvider>
)

describe('Authenticate component', () => {
  it('renders the Authenticate component with authenticated ', () => {
    jest
      .spyOn(Auth0, 'useAuth0')
      .mockReturnValue({ isAuthenticated: true, isLoading: false, getAccessTokenSilently: jest.fn() } as any)
    render(<AuthenticateDom />)
    expect(screen.getByText('childrendom')).toBeInTheDocument()
  })

  it('renders the Authenticate component without authenticated ', () => {
    jest
      .spyOn(Auth0, 'useAuth0')
      .mockReturnValue({ isAuthenticated: false, isLoading: false, getAccessTokenSilently: jest.fn() } as any)
    render(<AuthenticateDom />)
    expect(mockedUsedNavigate).toHaveBeenCalledTimes(1)
    expect(mockedUsedNavigate).toHaveBeenCalledWith(`${ENV.AUTH0_MGMT_ORG_ID}/login`)
  })
})
