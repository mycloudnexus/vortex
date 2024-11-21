import { act, fireEvent, render, waitFor } from '@testing-library/react'

import { MemoryRouter } from 'react-router-dom'
import CustomerCompany from '..'
import { ReactElement } from 'react'
import { QueryClient, QueryClientProvider } from 'react-query'

global.matchMedia = jest.fn().mockImplementation((query) => ({
  matches: false,
  media: query,
  addListener: jest.fn(),
  removeListener: jest.fn()
}))

jest.mock('@/assets/icon/customer-company.svg', () => ({
  ReactComponent: () => <svg data-testid='customer-company' />
}))
jest.mock('@/assets/icon/customer-company-empty.svg', () => ({
  ReactComponent: () => <svg data-testid='customer-company-empty' />
}))
jest.mock('@/assets/icon/status.svg', () => ({
  ReactComponent: () => <svg data-testid='status' />
}))
jest.mock('@/assets/icon/close-circle.svg', () => ({
  ReactComponent: () => <svg data-testid='close-circle' />
}))
jest.mock('@/assets/icon/warning-circle.svg', () => ({
  ReactComponent: () => <svg data-testid='warning-circle' />
}))
jest.mock('@/assets/icon/info.svg', () => ({
  ReactComponent: () => <svg data-testid='info' />
}))

jest.mock('@/hooks/company', () => ({
  useGetCompanyList: jest.fn(),
  useAddOrganization: jest.fn()
}))

const mockedUseGetCompanyList = require('@/hooks/company').useGetCompanyList
const mockedUseAddOrganization = require('@/hooks/company').useAddOrganization
const dummyData = {
  code: 200,
  message: 'OK',
  data: {
    data: [
      {
        name: 'helloworld123',
        id: 'org_D4ES55BSeeAHystq',
        display_name: 'hello_world',
        metadata: {
          loginType: 'undefined',
          status: 'ACTIVE',
          type: 'CUSTOMER'
        }
      },
      {
        name: 'qetesting',
        id: 'org_AeltT2tTQsOFNvCC',
        display_name: 'QE Testing'
      }
    ],
    total: 2,
    page: 0,
    size: 20
  }
}
describe('Customer Company Page', () => {
  let component: ReactElement
  const queryClient = new QueryClient()
  beforeEach(() => {
    jest.clearAllMocks()

    mockedUseAddOrganization.mockReturnValue({
      mutate: jest.fn()
    })
    component = (
      <QueryClientProvider client={queryClient}>
        <MemoryRouter>
          <CustomerCompany />
        </MemoryRouter>
      </QueryClientProvider>
    )
  })
  it('should render table columns', () => {
    mockedUseGetCompanyList.mockReturnValue({
      data: [],
      isLoading: false,
      isError: false
    })
    const { getByText } = render(component)

    expect(getByText('Name')).toBeInTheDocument()
    expect(getByText('ID')).toBeInTheDocument()
    expect(getByText('Short name & URL')).toBeInTheDocument()
    expect(getByText('Status')).toBeInTheDocument()
    expect(getByText('Action')).toBeInTheDocument()
  })

  it('form renders correctly and modal opens', async () => {
    const { getByTestId, getByText, getByLabelText, baseElement } = render(component)
    const button = getByTestId('add-button')
    expect(button).toBeInTheDocument()
    fireEvent.click(button)
    await waitFor(() => {
      expect(getByTestId('add-modal')).toBeInTheDocument()
    })
    act(() => {
      fireEvent.change(getByLabelText(/Customer company name/i), { target: { value: 'My Company' } })
      fireEvent.change(getByLabelText(/Customer company URL short name/i), { target: { value: 'MC' } })
    })
    const submitButton = getByText('OK')
    act(() => {
      fireEvent.click(submitButton)
    })
    expect(baseElement).toMatchSnapshot()
  })

  it('should display inactive company data in table rows', () => {
    mockedUseGetCompanyList.mockReturnValue({
      data: dummyData,
      isLoading: false,
      isError: false
    })
    const { getByText, baseElement } = render(component)
    expect(getByText('QE Testing')).toBeInTheDocument()
    expect(getByText('qetesting')).toBeInTheDocument()
    expect(getByText('org_AeltT2tTQsOFNvCC')).toBeInTheDocument()
    expect(getByText('Inactive')).toBeInTheDocument()
    expect(baseElement).toMatchSnapshot()
  })

  it('should call handleDeactivate when clicking Deactivate button', async () => {
    const { getByTestId, getByText } = render(component)
    fireEvent.click(getByTestId('handle-deactivate'))
    await waitFor(() => {
      expect(getByTestId('deactivate-modal')).toBeInTheDocument()
    })
    const submitButton = getByText('Yes, continue')
    fireEvent.click(submitButton)
  })
  it('should call handleActivate when clicking Activate button', () => {
    const { getByTestId } = render(component)
    fireEvent.click(getByTestId('handle-activate'))
  })
  it('form renders correctly and cancel submit', async () => {
    const { getByTestId, getByText } = render(component)
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
    const { getByTestId, getByLabelText, getByText } = render(component)
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
    const { getByTestId, getByText } = render(component)
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
