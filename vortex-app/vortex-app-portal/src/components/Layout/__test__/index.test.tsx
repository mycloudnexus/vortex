import { render, act, screen, fireEvent } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from 'react-query'
import Layout, { SliderCustom } from '..'
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
      setCustomerCompanies: jest.fn(),
      setCurrentCompany: jest.fn()
    }
  }
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
  it('Layout component with menu', async () => {
    const { container } = await act(async () => render(<BaseLayout />))
    expect(container).toBeInTheDocument()
    const dcPortsMenuBtn = screen.getByText('DC Ports')
    fireEvent.click(dcPortsMenuBtn)
    expect(screen.getByText('View all')).toBeInTheDocument()
    expect(screen.getByText('Add new')).toBeInTheDocument()
    const dashboardItem = screen.getAllByText('Dashboard')[0]
    fireEvent.click(dashboardItem)
    expect(dashboardItem.closest('li')?.classList.contains('ant-menu-item-selected'))
    fireEvent.click(screen.getByText('View all'))
    expect(screen.getByText('View all').closest('li')?.classList.contains('ant-menu-item-selected'))
    fireEvent.click(screen.getByText('Add new'))
    expect(screen.getByText('Add new').closest('li')?.classList.contains('ant-menu-item-selected'))
  })
})
describe('Layout component', () => {
  beforeEach(() => {
    jest
      .spyOn(Auth0, 'useAuth0')
      .mockReturnValue({ isAuthenticated: true, isLoading: false, user: {}, getAccessTokenSilently: jest.fn() } as any)
  })
  it('renders the layout component without authenticated ', async () => {
    const { container } = await act(async () => render(<BaseLayout />))
    expect(container.firstChild).toMatchSnapshot()
  })
})

test('SliderCustom component', async () => {
  const { container } = render(<SliderCustom $mainColor='#ff0000' />)
  expect(container).toBeInTheDocument()
})
