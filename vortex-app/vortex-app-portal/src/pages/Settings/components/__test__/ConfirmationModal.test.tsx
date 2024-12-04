import { render } from '@testing-library/react'
import ConfirmationModal from '../ConfirmationModal'

jest.mock('@/assets/icon/warning-circle.svg', () => ({
  ReactComponent: () => <svg data-testid></svg>
}))

describe('Confirmation Modal', () => {
  it('should render the component', () => {
    const { getByText } = render(
      <ConfirmationModal handleCancel={jest.fn()} handleOk={jest.fn()} open={true} text='test the text' />
    )
    expect(getByText(/test the text/i)).toBeInTheDocument()
  })
})
