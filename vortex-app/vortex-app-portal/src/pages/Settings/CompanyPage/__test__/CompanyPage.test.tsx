import { render } from '@testing-library/react'
import CompanyPage from '..'
import { MemoryRouter } from 'react-router-dom'

describe('Users Component', () => {
  it('should render the component', () => {
    const { baseElement } = render(
      <MemoryRouter>
        <CompanyPage />
      </MemoryRouter>
    )
    expect(baseElement).toMatchSnapshot()
  })
})
