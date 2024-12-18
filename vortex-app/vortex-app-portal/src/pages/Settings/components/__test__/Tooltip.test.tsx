import { render, fireEvent } from '@testing-library/react'
import Tooltip from '../Tooltip'

jest.mock('@/assets/icon/info.svg', () => ({
  ReactComponent: () => <svg />
}))

describe('Tooltip Component', () => {
  it('renders the Login URL with shortName', async () => {
    const { getByTestId, findByText } = render(<Tooltip orgId='example' />)

    const icon = getByTestId('mocked-svg')
    expect(icon).toBeInTheDocument()

    fireEvent.mouseOver(icon)

    const tooltipText = await findByText(/Login URL:/i)
    expect(tooltipText).toBeInTheDocument()

    const url = getByTestId('tooltip')
    expect(url).toBeInTheDocument()
  })

  it('renders the message when shortName is not defined', async () => {
    const { getByTestId, findByText } = render(<Tooltip message='test message' />)
    const icon = getByTestId('mocked-svg')
    expect(icon).toBeInTheDocument()

    fireEvent.mouseOver(icon)

    const tooltipText = await findByText(/test message/i)
    expect(tooltipText).toBeInTheDocument()
  })
})
