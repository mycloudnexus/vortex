import { render, fireEvent, waitFor } from '@testing-library/react'
import { Form } from 'antd'
import CustomerCompanyModal from '../CustomerModal'
import { ICompany } from '@/services/types'

jest.mock('@/assets/icon/info.svg', () => ({
  ReactComponent: () => <svg />
}))

global.matchMedia =
  global.matchMedia ||
  function () {
    return {
      matches: false,
      addListener: jest.fn(),
      removeListener: jest.fn()
    }
  }

const mockCompanies: ICompany[] = [
  {
    branding: {
      colors: { page_background: '', primary: '' },
      logo_url: ''
    },
    display_name: 'Company A',
    id: '123',
    metadata: {
      loginType: '',
      status: 'active',
      type: ''
    },
    name: 'test'
  },
  {
    branding: {
      colors: { page_background: '', primary: '' },
      logo_url: ''
    },
    display_name: 'Company B',
    id: '2',
    metadata: {
      loginType: '',
      status: '',
      type: ''
    },
    name: 'test'
  }
]

interface FormWrapperProps {
  handleOk: () => void
  handleCancel: () => void
  type?: 'add' | 'update'
}

const FormWrapper = ({ handleOk, handleCancel, type = 'add' }: FormWrapperProps) => {
  const [form] = Form.useForm()

  return (
    <CustomerCompanyModal
      title='Modify customer'
      name='modify_customer_company'
      companies={mockCompanies}
      form={form}
      handleCancel={handleCancel}
      handleOk={handleOk}
      initialValues={{ title: '', shortName: '' }}
      isModalOpen={true}
      type={type}
    />
  )
}

describe('CustomerCompanyModal', () => {
  it('renders the modal correctly with title and inputs', () => {
    const { getByText, getByLabelText, baseElement } = render(
      <FormWrapper handleOk={jest.fn()} handleCancel={jest.fn()} />
    )

    expect(getByText('Modify customer')).toBeInTheDocument()
    expect(getByLabelText(/Customer name/)).toBeInTheDocument()
    expect(getByLabelText(/Customer URL short name/)).toBeInTheDocument()
    expect(baseElement).toMatchSnapshot()
  })

  it('should call handleSubmit when the OK button is clicked', () => {
    const handleSubmitMock = jest.fn()
    const { getByText } = render(<FormWrapper handleCancel={jest.fn()} handleOk={handleSubmitMock} />)
    fireEvent.click(getByText('OK'))
    expect(handleSubmitMock).toHaveBeenCalledTimes(1)
  })
  it('should call handleCancel when the cancel button is clicked', () => {
    const handleCancelMock = jest.fn()
    const { getByRole } = render(<FormWrapper handleCancel={handleCancelMock} handleOk={jest.fn()} />)
    fireEvent.click(getByRole('button', { name: /cancel/i }))
    expect(handleCancelMock).toHaveBeenCalledTimes(1)
  })

  it('should disable the short name input when type is "update"', () => {
    const { getByLabelText } = render(<FormWrapper handleOk={jest.fn()} handleCancel={jest.fn()} type='update' />)
    expect(getByLabelText(/Customer URL short name/)).toBeDisabled()
  })

  it('should show an error message if the customer name is unique', async () => {
    const { getByLabelText, getByText, getByTestId } = render(
      <FormWrapper handleOk={jest.fn()} handleCancel={jest.fn()} />
    )

    fireEvent.change(getByTestId('customer-name'), { target: { value: 'Company B' } })
    expect(getByLabelText(/Customer name/)).toBeInTheDocument()
    fireEvent.click(getByText('OK'))

    await waitFor(() => {
      expect(getByText(/Customer name cannot be duplicated/i)).toBeInTheDocument()
    })
  })

  it('should not show an error message if the customer name is unique', async () => {
    const { getByLabelText, getByText, getByTestId, queryByText } = render(
      <FormWrapper handleOk={jest.fn()} handleCancel={jest.fn()} />
    )

    fireEvent.change(getByTestId('customer-name'), { target: { value: 'Company C' } })
    expect(getByLabelText(/Customer name/)).toBeInTheDocument()
    fireEvent.click(getByText('OK'))

    await waitFor(() => {
      expect(queryByText(/Customer name cannot be duplicated/i)).toBeNull()
    })
  })
})
