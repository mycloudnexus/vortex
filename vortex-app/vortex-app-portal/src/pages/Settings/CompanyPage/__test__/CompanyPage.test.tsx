import { render } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import CompanyPage from '..'
import { ReactElement } from 'react'
import { QueryClient, QueryClientProvider } from 'react-query'

jest.mock('@/assets/icon/lock.svg', () => ({
  ReactComponent: () => <svg data-testid='lock-icon'></svg>
}))

jest.mock('@/assets/icon/sso.svg', () => ({
  ReactComponent: () => <svg data-testid='lock-icon'></svg>
}))

jest.mock('@/assets/icon/customer-company.svg', () => ({
  ReactComponent: () => <svg data-testid='lock-icon'></svg>
}))

jest.mock('@/assets/icon/info.svg', () => ({
  ReactComponent: () => <svg data-testid='info-icon'></svg>
}))

jest.mock('@/assets/icon/warning-circle.svg', () => ({
  ReactComponent: () => <svg data-testid></svg>
}))

jest.mock('@/hooks/company', () => ({
  useGetOrganizationById: jest.fn()
}))

const mockedGetOrganizationById = require('@/hooks/company').useGetOrganizationById
const dummyData = {
  code: 200,
  message: 'OK',
  data: {
    name: 'davexiongtest',
    id: 'org_qbPaadGBsyy2VMok',
    display_name: 'DaveXiong-Test',
    metadata: {
      loginType: 'undefined',
      status: 'ACTIVE',
      type: 'CUSTOMER'
    },
    enabled_connections: []
  }
}
describe('Company Page Component', () => {
  let component: ReactElement
  let queryClient = new QueryClient()

  beforeEach(() => {
    component = (
      <QueryClientProvider client={queryClient}>
        <MemoryRouter>
          <CompanyPage />
        </MemoryRouter>
      </QueryClientProvider>
    )
  })

  afterEach(() => {
    jest.clearAllMocks()
  })

  it('should render the component', () => {
    mockedGetOrganizationById.mockReturnValue({
      data: dummyData
    })
    const { getByText } = render(component)
    expect(getByText(/DaveXiong-Test/i)).toBeInTheDocument()
  })

  it('should check the loading state', () => {
    mockedGetOrganizationById.mockReturnValue({
      data: null,
      isFetching: true
    })
    const { getByText } = render(component)
    expect(getByText(/Loading/i)).toBeInTheDocument()
  })
})
