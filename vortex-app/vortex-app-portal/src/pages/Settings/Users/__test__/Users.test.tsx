import { render } from '@testing-library/react'
import Users from '..'

describe('Users Component', () => {
  it('should render the component', () => {
    const { baseElement } = render(<Users />)
    expect(baseElement).toMatchSnapshot()
  })
})
