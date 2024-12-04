import { render } from '@testing-library/react'
import CardTab from '../CardTab'

describe('CardTab Component', () => {
  it('should render the component', () => {
    const { getByText, getByTestId } = render(
      <CardTab
        description='test description'
        isEnabled
        title='test title'
        icon={<svg></svg>}
        titleExtension='test extension'
      />
    )
    const title = getByText(/test title/i)
    const description = getByText(/test description/i)
    const enabled = getByText(/Enabled/i)
    const titleExtension = getByText(/test extension/i)
    const icon = getByTestId('tab-icon')

    expect(title).toBeInTheDocument()
    expect(description).toBeInTheDocument()
    expect(enabled).toBeInTheDocument()
    expect(titleExtension).toBeInTheDocument()
    expect(icon).toBeInTheDocument()
  })
})
