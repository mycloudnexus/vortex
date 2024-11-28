import { fireEvent, render, waitFor } from '@testing-library/react'

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
  useAddOrganization: jest.fn(),
  useUpdateOrganization: jest.fn()
}))

const mockedUseGetCompanyList = require('@/hooks/company').useGetCompanyList
const mockedUseAddOrganization = require('@/hooks/company').useAddOrganization
const mockedUseUpdateOrganization = require('@/hooks/company').useUpdateOrganization
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
  jest.spyOn(queryClient, 'setQueryData')
  beforeEach(() => {
    mockedUseAddOrganization.mockReturnValue({
      mutate: jest.fn()
    })

    mockedUseUpdateOrganization.mockReturnValue({
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

  afterEach(() => {
    jest.clearAllMocks()
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
    //will update this test
    const { getByTestId, getByText } = render(component)
    fireEvent.click(getByTestId('handle-deactivate'))
    await waitFor(() => {
      expect(getByTestId('deactivate-modal')).toBeInTheDocument()
    })
    const submitButton = getByText('Yes, continue')
    fireEvent.click(submitButton)
  })

  it('should call handleActivate when clicking Activate button', () => {
    //will update this test
    const { getByTestId } = render(component)
    fireEvent.click(getByTestId('handle-activate'))
  })
  it('add form renders correctly and cancel submit', async () => {
    //Will update this test
    const { getByTestId, getByText, queryByTestId } = render(component)
    const button = getByTestId('add-button')
    expect(button).toBeInTheDocument()
    fireEvent.click(button)
    await waitFor(() => {
      expect(getByTestId('add-modal')).toBeInTheDocument()
    })
    const submitButton = getByText('Cancel')
    fireEvent.click(submitButton)
    await waitFor(() => {
      expect(queryByTestId('add-modal')).not.toBeInTheDocument()
    })
  })

  // Helper function to handle form submission
  const handleFormSubmitForUpdate = async ({ companyName }: { companyName: string }) => {
    const { getByTestId, getByLabelText, getByText } = render(component)

    const button = getByTestId('handle-modify')
    expect(button).toBeInTheDocument()

    fireEvent.click(button)

    await waitFor(() => {
      expect(getByTestId('update-modal')).toBeInTheDocument()
    })

    fireEvent.change(getByLabelText(/Customer company name/i), { target: { value: companyName } })

    const submitButton = getByText('OK')
    fireEvent.click(submitButton)

    return { getByTestId, getByLabelText, getByText }
  }

  it('should call useUpdateOrganization mutation on form submission', async () => {
    const mockMutate = jest.fn((_data, { onSuccess }) => {
      onSuccess({
        data: {
          id: 'org_D4ES55BSeeAHystq',
          display_name: 'My Company'
        }
      })
    })

    mockedUseUpdateOrganization.mockReturnValue({
      mutate: mockMutate
    })

    await handleFormSubmitForUpdate({ companyName: 'My Company' })

    await waitFor(() => {
      expect(mockMutate).toHaveBeenCalledWith(
        expect.objectContaining({
          id: 'org_D4ES55BSeeAHystq',
          request_body: {
            display_name: 'My Company'
          }
        }),
        expect.objectContaining({
          onSuccess: expect.any(Function),
          onError: expect.any(Function)
        })
      )
    })

    await waitFor(() => {
      expect(mockMutate).toHaveBeenCalledTimes(1)
    })
  })

  it('should handle useUpdateOrganization onError callback', async () => {
    const mockMutate = jest.fn((_data, { onError }) => {
      const mockError = new Error('Network error')
      onError(mockError)
    })

    const consoleLogSpy = jest.spyOn(console, 'log').mockImplementation(() => {})

    mockedUseUpdateOrganization.mockReturnValue({
      mutate: mockMutate
    })

    await handleFormSubmitForUpdate({ companyName: 'My Company' })

    await waitFor(() => {
      expect(mockMutate).toHaveBeenCalledWith(
        expect.objectContaining({
          id: 'org_D4ES55BSeeAHystq',
          request_body: {
            display_name: 'My Company'
          }
        }),
        expect.objectContaining({
          onSuccess: expect.any(Function),
          onError: expect.any(Function)
        })
      )
    })

    await waitFor(() => {
      expect(consoleLogSpy).toHaveBeenCalledWith(expect.any(Error), 'error update')
    })

    expect(mockMutate).toHaveBeenCalledTimes(1)

    consoleLogSpy.mockRestore()
  })

  it('form update renders correctly and cancel submit', async () => {
    const { getByTestId, getByText, queryByTestId } = render(component)
    const button = getByTestId('handle-modify')
    expect(button).toBeInTheDocument()
    fireEvent.click(button)
    await waitFor(() => {
      expect(getByTestId('update-modal')).toBeInTheDocument()
    })
    const cancelButton = getByText('Cancel')
    fireEvent.click(cancelButton)
    await waitFor(() => {
      expect(queryByTestId('update-modal')).not.toBeInTheDocument()
    })
  })

  // Helper function to handle form submission
  const handleFormSubmit = async ({ companyName, companyUrl }: { companyName: string; companyUrl: string }) => {
    const { getByTestId, getByLabelText, getByText } = render(component)

    fireEvent.click(getByTestId('add-button'))

    await waitFor(() => {
      expect(getByTestId('add-modal')).toBeInTheDocument()
    })

    fireEvent.change(getByLabelText(/Customer company name/i), {
      target: { value: companyName }
    })
    fireEvent.change(getByLabelText(/Customer company URL short name/i), {
      target: { value: companyUrl }
    })
    fireEvent.click(getByText('OK'))

    return { getByTestId, getByLabelText, getByText }
  }

  it('should call useAddOrganization mutation on form submission', async () => {
    const mockMutate = jest.fn((data, { onSuccess }) => {
      onSuccess({
        data: {
          display_name: data.display_name,
          name: data.name.toLowerCase()
        }
      })
    })

    mockedUseAddOrganization.mockReturnValue({
      mutate: mockMutate
    })

    await handleFormSubmit({ companyName: 'My New Company', companyUrl: 'abc' })

    await waitFor(() => {
      expect(mockMutate).toHaveBeenCalledWith(
        expect.objectContaining({
          display_name: 'My New Company',
          name: 'abc'
        }),
        expect.objectContaining({
          onSuccess: expect.any(Function),
          onError: expect.any(Function)
        })
      )
    })
  })

  it('should handle useAddOrganization onError callback', async () => {
    const mockMutate = jest.fn((_data, { onError }) => {
      const mockError = new Error('Network error')
      onError(mockError)
    })

    const consoleLogSpy = jest.spyOn(console, 'log').mockImplementation(() => {})
    mockedUseAddOrganization.mockReturnValue({
      mutate: mockMutate
    })

    await handleFormSubmit({ companyName: 'My New Company', companyUrl: 'abc' })

    await waitFor(() => {
      expect(mockMutate).toHaveBeenCalledWith(
        expect.objectContaining({
          display_name: 'My New Company',
          name: 'abc'
        }),
        expect.objectContaining({
          onSuccess: expect.any(Function),
          onError: expect.any(Function)
        })
      )
    })

    await waitFor(() => {
      expect(consoleLogSpy).toHaveBeenCalledWith(expect.any(Error), 'error adding')
    })
    expect(mockMutate).toHaveBeenCalledTimes(1)
    consoleLogSpy.mockRestore()
  })
})
