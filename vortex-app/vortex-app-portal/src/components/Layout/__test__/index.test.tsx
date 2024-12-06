import { render, act } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from 'react-query'
import Layout from '..'
import * as Auth0 from '@auth0/auth0-react'
import * as userHooks from '@/hooks/user'

jest.mock('@/utils/helpers/request')
const mockedUsedNavigate = jest.fn()
const queryClient = new QueryClient()

jest.mock('@auth0/auth0-react')
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockedUsedNavigate
}))
jest.mock('@/stores/app.store', () => ({
  useAppStore: () => {
    return {
      userType: 'reseller',
      currentAuth0User: {},
      setRoleList: jest.fn(),
      setDownstreamUser: jest.fn(),
      setCustomerUser: jest.fn(),
      setCustomerCompanies: jest.fn()
    }
  }
}))

jest.mock('@/assets/icon/dashboard.svg', () => ({
  ReactComponent: () => <svg />
}))
jest.mock('@/assets/icon/dcport.svg', () => ({
  ReactComponent: () => <svg />
}))
jest.mock('@/assets/icon/cloudrouter.svg', () => ({
  ReactComponent: () => <svg />
}))
jest.mock('@/assets/icon//l2.svg', () => ({
  ReactComponent: () => <svg />
}))
jest.mock('@/assets/icon/l3.svg', () => ({
  ReactComponent: () => <svg />
}))
jest.mock('@/assets/icon/setting.svg', () => ({
  ReactComponent: () => <svg />
}))
jest
  .spyOn(Auth0, 'useAuth0')
  .mockReturnValue({ isAuthenticated: true, isLoading: false, user: {}, getAccessTokenSilently: jest.fn() } as any)

const BaseLayout = () => (
  <QueryClientProvider client={queryClient}>
    <BrowserRouter>
      <Layout />
    </BrowserRouter>
  </QueryClientProvider>
)

describe('Layout component with data', () => {
  const testName = 'test'
  const testEmail = 'test@test.com'
  beforeEach(() => {
    jest.spyOn(userHooks, 'useGetUserAuthDetail').mockReturnValue({
      data: {
        data: {
          name: testName,
          email: testEmail
        }
      }
    } as any)
  })
  it('save user data after get data ', async () => {
    await act(async () => render(<BaseLayout />))
    const userName = window.portalLoggedInUser.name
    const userEmail = window.portalLoggedInUser.email
    expect(userName).toEqual(testName)
    expect(userEmail).toEqual(testEmail)
  })
})
// describe('Layout component', () => {
//   beforeEach(() => {
//     jest
//       .spyOn(Auth0, 'useAuth0')
//       .mockReturnValue({ isAuthenticated: true, isLoading: false, user: {}, getAccessTokenSilently: jest.fn() } as any)
//   })
//   it('renders the layout component with authenticated ', async () => {
//     const { container } = await act(async () => render(<BaseLayout />))
//     expect(container.firstChild).toMatchSnapshot()
//   })
// })
