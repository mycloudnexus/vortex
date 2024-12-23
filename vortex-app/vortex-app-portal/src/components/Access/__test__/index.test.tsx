import React from 'react'
import { render, screen } from '@testing-library/react'
import { Auth0Provider } from '@auth0/auth0-react'
import { ENV } from '@/constant'
import { AuthProvider } from '..'

const mockedUsedNavigate = jest.fn()

jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockedUsedNavigate
}))

jest.mock('@auth0/auth0-react', () => ({
  Auth0Provider: jest.fn()
}))

const mockAuth0Provider = Auth0Provider as jest.MockedFunction<typeof Auth0Provider>
describe('auth reseller provider', () => {
  beforeEach(() => {
    jest.mock('@/stores/app.store', () => ({
      useAppStore: () => {
        return {
          userType: 'reseller'
        }
      }
    }))
    mockAuth0Provider.mockClear().mockImplementation(({ children }) => children as React.ReactElement)
    ENV.RESELLER_AUTH0_AUDIENCE = 'audience'
    ENV.RESELLER_AUTH0_CLIENT_ID = 'clientId'
    ENV.RESELLER_AUTH0_DOMAIN = 'https://auth0.domain.test'
  })
  const Children = <span>children</span>
  it('renders the Auth0 Provider', () => {
    expect.hasAssertions()

    render(<AuthProvider>{Children}</AuthProvider>)

    expect(screen.getByText('children')).toBeInTheDocument()
  })
})

describe('auth customer provider', () => {
  beforeEach(() => {
    jest.mock('@/stores/app.store', () => ({
      useAppStore: () => {
        return {
          userType: 'customer'
        }
      }
    }))
    mockAuth0Provider.mockClear().mockImplementation(({ children }) => children as React.ReactElement)
    ENV.CUSTOMER_AUTH0_AUDIENCE = 'CUSTOMER_audience'
    ENV.CUSTOMER_AUTH0_CLIENT_ID = 'CUSTOMER_clientId'
    ENV.CUSTOMER_AUTH0_DOMAIN = 'https://auth0.customer.domain.test'
  })
  const Children = <span>children</span>
  it('renders the customer Auth0 Provider', () => {
    expect.hasAssertions()

    render(<AuthProvider>{Children}</AuthProvider>)

    expect(screen.getByText('children')).toBeInTheDocument()
  })
})
