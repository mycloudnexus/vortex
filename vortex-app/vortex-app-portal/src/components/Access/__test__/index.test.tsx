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
describe('auth provider', () => {
  beforeEach(() => {
    mockAuth0Provider.mockClear().mockImplementation(({ children }) => children as React.ReactElement)
    ENV.AUTH0_AUDIENCE = 'audience'
    ENV.AUTH0_CLIENT_ID = 'clientId'
    ENV.AUTH0_DOMAIN = 'https://auth0.domain.test'
  })
  const Children = <span>children</span>
  it('renders the Auth0 Provider', () => {
    expect.hasAssertions()

    render(<AuthProvider>{Children}</AuthProvider>)

    expect(screen.getByText('children')).toBeInTheDocument()
  })
})
