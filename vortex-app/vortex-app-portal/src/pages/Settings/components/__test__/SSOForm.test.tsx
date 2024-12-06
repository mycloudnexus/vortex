import { act, fireEvent, render } from '@testing-library/react'
import SSOForm from '../SSOForm'
import { ReactElement } from 'react'
import { QueryClient, QueryClientProvider } from 'react-query'
import { MemoryRouter } from 'react-router-dom'

global.matchMedia = jest.fn().mockImplementation((query) => ({
  matches: false,
  media: query,
  addListener: jest.fn(),
  removeListener: jest.fn()
}))
jest.mock('@/assets/icon/warning-circle.svg', () => ({
  ReactComponent: () => <svg data-testid='close'></svg>
}))

const uploadFile = async (uploadInput: HTMLInputElement, file: File) => {
  expect(uploadInput).toBeInTheDocument()
  await act(async () => {
    fireEvent.change(uploadInput, { target: { files: [file] } })
  })
  expect(uploadInput.files?.[0]).toEqual(file)
  expect(uploadInput.files?.[0]?.name).toBe(file.name)
}

const enterSSOUrl = async (ssoUrlInput: HTMLInputElement, url: string) => {
  await act(async () => {
    fireEvent.change(ssoUrlInput, { target: { value: url } })
  })
  expect(ssoUrlInput.value).toBe(url)
}

describe('SSO Form', () => {
  let component: ReactElement
  const queryClient = new QueryClient()
  const file = new File(['dummy content'], 'example.pem', { type: 'application/x-pem-file' })

  beforeEach(() => {
    component = (
      <QueryClientProvider client={queryClient}>
        <MemoryRouter>
          <SSOForm loginMethod='' />
        </MemoryRouter>
      </QueryClientProvider>
    )
  })

  afterEach(() => {
    jest.clearAllMocks()
  })

  it('should fire event on upload', async () => {
    const { getByTestId } = render(component)
    const uploadInput = getByTestId('upload-docs') as HTMLInputElement
    await uploadFile(uploadInput, file)
  })

  it('should enable and disable submit button', async () => {
    const { getByTestId } = render(component)
    const submitButton = getByTestId('submit-button')
    const ssoUrlInput = getByTestId('sso-url') as HTMLInputElement
    const uploadInput = getByTestId('upload-docs') as HTMLInputElement

    expect(submitButton).toBeDisabled()

    await enterSSOUrl(ssoUrlInput, 'https://example.com/sso')
    await uploadFile(uploadInput, file)

    expect(submitButton).toBeEnabled()

    await act(async () => {
      fireEvent.click(submitButton)
    })
    expect(submitButton).toBeDisabled()
  })

  it('should open warning modal', async () => {
    const { getByTestId, getByText } = render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter>
          <SSOForm loginMethod='email' />
        </MemoryRouter>
      </QueryClientProvider>
    )
    const submitButton = getByTestId('submit-button')
    const ssoUrlInput = getByTestId('sso-url') as HTMLInputElement
    const uploadInput = getByTestId('upload-docs') as HTMLInputElement

    expect(submitButton).toBeDisabled()

    await enterSSOUrl(ssoUrlInput, 'https://example.com/sso')
    await uploadFile(uploadInput, file)

    expect(submitButton).toBeEnabled()

    await act(async () => {
      fireEvent.click(submitButton)
    })
    expect(getByTestId('warning-modal')).toBeInTheDocument()

    const modalSubmit = getByText(/Yes, continue/i)
    await act(async () => {
      fireEvent.click(modalSubmit)
    })
    expect(submitButton).toBeDisabled()
  })

  it('should show sign request', async () => {
    const { getByTestId } = render(component)
    const switchElement = getByTestId('switch-request') as HTMLInputElement
    expect(switchElement).not.toBeChecked()
    await act(async () => {
      fireEvent.click(switchElement)
    })
    expect(switchElement).toBeChecked()
  })
})
