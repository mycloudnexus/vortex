import { render, screen } from '@testing-library/react'
import Authenticate from '../Authenticate'
import { ENV } from '@/constant'
import * as Auth0 from '@auth0/auth0-react'

const mockedUsedNavigate = jest.fn()

jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockedUsedNavigate
}))
jest.mock('@auth0/auth0-react')

describe('Authenticate component', () => {
  const Children = <span>childrendom</span>
  it('renders the Authenticate component with authenticated ', () => {
    jest.spyOn(Auth0, 'useAuth0').mockReturnValue({ isAuthenticated: true, isLoading: false } as any)
    render(<Authenticate>{Children}</Authenticate>)
    expect(screen.getByText('childrendom')).toBeInTheDocument()
  })

  it('renders the Authenticate component without authenticated ', () => {
    jest.spyOn(Auth0, 'useAuth0').mockReturnValue({ isAuthenticated: false, isLoading: false } as any)
    render(<Authenticate>{Children}</Authenticate>)
    expect(mockedUsedNavigate).toHaveBeenCalledTimes(1)
    expect(mockedUsedNavigate).toHaveBeenCalledWith(`${ENV.AUTH0_MGMT_ORG_ID}/login`)
  })
})
