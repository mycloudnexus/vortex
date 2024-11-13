import { fireEvent, render, waitFor } from '@testing-library/react'

import { MemoryRouter } from 'react-router-dom'
import CustomerCompany from '..'

global.matchMedia = jest.fn().mockImplementation((query) => ({
  matches: false,
  media: query,
  addListener: jest.fn(),
  removeListener: jest.fn()
}))
jest.mock('@/assets/icon/customer-company.svg', () => ({
  ReactComponent: () => <svg />
}))
jest.mock('@/assets/icon/customer-company-empty.svg', () => ({
  ReactComponent: () => <svg />
}))
jest.mock('@/assets/icon/status.svg', () => ({
  ReactComponent: () => <svg />
}))
jest.mock('@/assets/icon/close-circle.svg', () => ({
  ReactComponent: () => <svg />
}))
jest.mock('@/assets/icon/warning-circle.svg', () => ({
  ReactComponent: () => <svg />
}))
jest.mock('@/assets/icon/info.svg', () => ({
  ReactComponent: () => <svg />
}))
jest.mock('@/stores/company.store', () => ({
  useCompanyStore: jest.fn().mockReturnValue({
    companies: [
      {
        key: '1',
        id: '1',
        title: 'Test Company',
        shortName: 'TC',
        status: 'active'
      },
      {
        key: '2',
        id: '2',
        title: 'Inactive Company',
        shortName: 'IC',
        status: 'inactive'
      }
    ],
    addCompany: jest.fn(),
    updateCompanyRecord: jest.fn(),
    updateCompanyStatus: jest.fn()
  })
}))
describe('test', () => {
  it('should render the page', () => {
    const { baseElement } = render(
      <MemoryRouter>
        <CustomerCompany />
      </MemoryRouter>
    )
    expect(baseElement).toMatchSnapshot()
  })

  it('should render the company name', () => {
    const { getByText } = render(
      <MemoryRouter>
        <CustomerCompany />
      </MemoryRouter>
    )

    expect(getByText('Test Company')).toBeInTheDocument()
  })

  it('should render the company status', () => {
    const { getByText } = render(
      <MemoryRouter>
        <CustomerCompany />
      </MemoryRouter>
    )

    expect(getByText('Active')).toBeInTheDocument()
  })

  it('should render table columns', () => {
    const { getByText } = render(
      <MemoryRouter>
        <CustomerCompany />
      </MemoryRouter>
    )

    expect(getByText('Name')).toBeInTheDocument()
    expect(getByText('ID')).toBeInTheDocument()
    expect(getByText('Short name & URL')).toBeInTheDocument()
    expect(getByText('Status')).toBeInTheDocument()
    expect(getByText('Action')).toBeInTheDocument()
  })

  it('should display company data in table rows', () => {
    const { getByText } = render(
      <MemoryRouter>
        <CustomerCompany />
      </MemoryRouter>
    )
    expect(getByText('Test Company')).toBeInTheDocument()
    expect(getByText('1')).toBeInTheDocument()
    expect(getByText('TC')).toBeInTheDocument()
    expect(getByText('Active')).toBeInTheDocument()
  })

  it('should display company data in table rows', () => {
    const { getByText } = render(
      <MemoryRouter>
        <CustomerCompany />
      </MemoryRouter>
    )
    expect(getByText('Test Company')).toBeInTheDocument()
    expect(getByText('1')).toBeInTheDocument()
    expect(getByText('TC')).toBeInTheDocument()
    expect(getByText('Active')).toBeInTheDocument()
  })

  it('should display inactive company data in table rows', () => {
    const { getByText } = render(
      <MemoryRouter>
        <CustomerCompany />
      </MemoryRouter>
    )
    expect(getByText('Inactive Company')).toBeInTheDocument()
    expect(getByText('2')).toBeInTheDocument()
    expect(getByText('IC')).toBeInTheDocument()
    expect(getByText('Inactive')).toBeInTheDocument()
  })
  it('should call handleDeactivate when clicking Deactivate button', async () => {
    const { getByTestId, getByText } = render(
      <MemoryRouter>
        <CustomerCompany />
      </MemoryRouter>
    )
    fireEvent.click(getByTestId('handle-deactivate'))
    await waitFor(() => {
      expect(getByTestId('deactivate-modal')).toBeInTheDocument()
    })
    const submitButton = getByText('Yes, continue')
    fireEvent.click(submitButton)
  })
  it('should call handleActivate when clicking Activate button', () => {
    const { getByTestId } = render(
      <MemoryRouter>
        <CustomerCompany />
      </MemoryRouter>
    )
    fireEvent.click(getByTestId('handle-activate'))
  })
  it('form renders correctly and modal opens', async () => {
    const { getByTestId, getByText, getByLabelText, baseElement } = render(
      <MemoryRouter>
        <CustomerCompany />
      </MemoryRouter>
    )
    const button = getByTestId('add-button')
    expect(button).toBeInTheDocument()
    fireEvent.click(button)
    await waitFor(() => {
      expect(getByTestId('add-modal')).toBeInTheDocument()
    })
    fireEvent.change(getByLabelText(/Customer company name/i), { target: { value: 'My Company' } })
    fireEvent.change(getByLabelText(/Customer company URL short name/i), { target: { value: 'MC' } })
    const submitButton = getByText('OK')
    fireEvent.click(submitButton)
    expect(baseElement).toMatchSnapshot()
  })
  it('form renders correctly and cancel submit', async () => {
    const { getByTestId, getByText } = render(
      <MemoryRouter>
        <CustomerCompany />
      </MemoryRouter>
    )
    const button = getByTestId('add-button')
    expect(button).toBeInTheDocument()
    fireEvent.click(button)
    await waitFor(() => {
      expect(getByTestId('add-modal')).toBeInTheDocument()
    })
    const submitButton = getByText('Cancel')
    fireEvent.click(submitButton)
  })
  it('form update renders correctly and modal opens', async () => {
    const { getByTestId, getByLabelText, getByText } = render(
      <MemoryRouter>
        <CustomerCompany />
      </MemoryRouter>
    )
    const button = getByTestId('handle-modify')
    expect(button).toBeInTheDocument()
    fireEvent.click(button)
    await waitFor(() => {
      expect(getByTestId('update-modal')).toBeInTheDocument()
    })
    fireEvent.change(getByLabelText(/Customer company name/i), { target: { value: 'My Company' } })
    const submitButton = getByText('OK')
    fireEvent.click(submitButton)
  })
  it('form update renders correctly and cancel submit', async () => {
    const { getByTestId, getByText } = render(
      <MemoryRouter>
        <CustomerCompany />
      </MemoryRouter>
    )
    const button = getByTestId('handle-modify')
    expect(button).toBeInTheDocument()
    fireEvent.click(button)
    await waitFor(() => {
      expect(getByTestId('update-modal')).toBeInTheDocument()
    })
    const submitButton = getByText('Cancel')
    fireEvent.click(submitButton)
  })
})
