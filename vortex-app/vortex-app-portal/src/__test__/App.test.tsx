import { render } from '@/test/setupTest'
import App from '../App'

jest.mock('@auth0/auth0-react', () => ({
  Auth0Provider: jest.fn()
}))

describe('App test', () => {
  it('renders  app ', () => {
    const { container } = render(<App />)
    expect(container).toBeInTheDocument()
  })
  it('renders  with favicon ', () => {
    const { container } = render(<App />)
    expect(container.firstChild).toMatchSnapshot()
  })
})
