import { render } from '@testing-library/react'
import CustomerCompany from '..'
import { MemoryRouter } from 'react-router-dom'

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
jest.mock('antd/es/form/Form', () => ({
  useForm: jest.fn()
}))
jest.mock('@/stores/app.store', () => ({
  useAppStore: jest.fn().mockReturnValue({ mainColor: '#000' })
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
      }
    ],
    addCompany: jest.fn(),
    updateCompanyRecord: jest.fn(),
    updateCompanyStatus: jest.fn()
  })
}))

jest.mock('antd/es/form/Form', () => ({
  useForm: jest.fn().mockReturnValue([{}, jest.fn()])
}))

global.matchMedia = jest.fn().mockImplementation((query) => ({
  matches: false,
  media: query,
  addListener: jest.fn(),
  removeListener: jest.fn()
}))

describe('CutomerCompany Page', () => {
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
})
