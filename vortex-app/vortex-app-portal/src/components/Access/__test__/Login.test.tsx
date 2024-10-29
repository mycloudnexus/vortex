import { render } from '@testing-library/react'
import { useAuth0 } from '@auth0/auth0-react'
import Login from '../Login'

const mockedUsedNavigate = jest.fn()
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockedUsedNavigate,
  useParams: jest.fn().mockReturnValue({ organization: '123456' })
}))
jest.mock('@auth0/auth0-react')
const fn = useAuth0 as any
describe('Login page', () => {
  it('redirect correct org', () => {
    const url = 'https://www.test.com'
    Object.defineProperty(window, 'location', {
      value: {
        origin: url
      },
      writable: true
    })

    const loginWithRedirect = jest.fn()
    fn.mockReturnValue({
      loginWithRedirect,
      isAuthenticated: false
    })

    render(<Login />)

    expect(loginWithRedirect).toHaveBeenCalledWith({
      authorizationParams: {
        organization: '123456',
        redirect_uri: url
      }
    })
  })
  it('redirect to home page', () => {
    fn.mockReturnValue({ isAuthenticated: true, user: {}, getAccessTokenSilently: jest.fn() } as any)
    render(<Login />)
    expect(mockedUsedNavigate).toHaveBeenCalledTimes(1)
    expect(mockedUsedNavigate).toHaveBeenCalledWith(`/`)
  })
})
