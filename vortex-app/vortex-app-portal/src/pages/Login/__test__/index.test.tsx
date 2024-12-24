import { render, fireEvent } from '@testing-library/react'
import { useAuth0 } from '@auth0/auth0-react'
import { MemoryRouter } from 'react-router-dom'

import Login from '..'

jest.mock('@auth0/auth0-react')

const mockedUsedNavigate = jest.fn()
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockedUsedNavigate,
  useParams: jest.fn().mockReturnValue({})
}))

const fn = useAuth0 as any
const LoginWithRouter = () => {
  return (
    <MemoryRouter>
      <Login />
    </MemoryRouter>
  )
}

describe('Login page', () => {
  it('login as a reseller', () => {
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
      isAuthenticated: false,
      error: false
    })
    const { getByText } = render(<LoginWithRouter />)
    const loginBtn = getByText('Log in')
    expect(loginBtn).toBeTruthy()
    fireEvent.click(loginBtn)

    expect(loginBtn).toBeTruthy()
    expect(loginWithRedirect).toHaveBeenCalledWith({
      authorizationParams: {
        redirect_uri: 'https://www.test.com/login'
      }
    })
  })
  it('login as a customer', () => {
    jest.mock('react-router-dom', () => ({
      useParams: jest.fn().mockReturnValue({ organization: 'orgid123' })
    }))
    const origin = 'https://www.test.com'
    const url = origin + '/orgid123/login'
    Object.defineProperty(window, 'location', {
      value: {
        href: url,
        origin
      },
      writable: true
    })
    const loginWithRedirect = jest.fn()
    fn.mockReturnValue({
      loginWithRedirect,
      isAuthenticated: false,
      error: false
    })
    const { getByText } = render(<LoginWithRouter />)
    const loginBtn = getByText('Log in')
    expect(loginBtn).toBeTruthy()
    fireEvent.click(loginBtn)

    expect(loginBtn).toBeTruthy()
    expect(loginWithRedirect).toHaveBeenCalledWith({
      authorizationParams: {
        redirect_uri: origin + '/login'
      }
    })
  })

  it('should redirect index page if have user', () => {
    fn.mockReturnValue({ isAuthenticated: true, user: {}, getAccessTokenSilently: jest.fn() } as any)
    render(<LoginWithRouter />)
    expect(mockedUsedNavigate).toHaveBeenCalledTimes(1)
    expect(mockedUsedNavigate).toHaveBeenCalledWith(`/`)
  })

  it('should show Skeleton if url includes code search params ', () => {
    const { container } = render(
      <MemoryRouter initialEntries={['/login?code=123']}>
        <Login />
      </MemoryRouter>
    )
    const ele = container.querySelector('.ant-skeleton')
    expect(container.firstChild).toMatchSnapshot()
    expect(ele).toBeTruthy()
  })
})
