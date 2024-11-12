import { render, fireEvent } from '@testing-library/react'
import Tooltip from '../Tooltip'

jest.mock('@/assets/icon/info.svg', () => ({
  ReactComponent: () => <svg />
}))

describe('Tooltip Component', () => {
  it('renders the Login URL with shortName', async () => {
    const { getByTestId, baseElement, getByText, findByText } = render(<Tooltip shortName='example' />)

    const icon = getByTestId('mocked-svg')
    expect(icon).toBeInTheDocument()

    fireEvent.mouseOver(icon)

    const tooltipText = await findByText(/Login URL:/i)
    expect(tooltipText).toBeInTheDocument()

    const url = getByText(/consoleconnect\/example\.com/i)
    expect(url).toBeInTheDocument()

    expect(baseElement).toMatchSnapshot()
  })

  it('renders the message when shortName is not defined', async () => {
    const { getByTestId, baseElement, findByText } = render(<Tooltip message='test message' />)
    const icon = getByTestId('mocked-svg')
    expect(icon).toBeInTheDocument()

    fireEvent.mouseOver(icon)

    const tooltipText = await findByText(/test message/i)
    expect(tooltipText).toBeInTheDocument()

    expect(baseElement).toMatchSnapshot()
  })
})
