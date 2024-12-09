import { render, renderHook, act, fireEvent, waitFor } from '@testing-library/react'
import { useAppStore } from '@/stores/app.store'
import type { ICompany } from '@/services/types'

import SettingMenu from '../SettingsMenu'
const mockDownstreamUser = {
  companies: [
    {
      name: 'mockResellerCompany',
      id: '123',
      display_name: 'mockResellerCompany'
    }
  ]
}
const mockCompanies = [
  {
    display_name: 'test',
    id: '21n9',
    name: 'test'
  },
  {
    display_name: 'test1',
    id: '21n92',
    name: 'test3'
  }
]

describe('NavMain component without company ', () => {
  it(`it shouldn't show reseller company name `, () => {
    const { queryByText } = render(<SettingMenu />)
    const elem = queryByText(/Viewing as/i)
    expect(elem).not.toBeInTheDocument()
  })
})
describe('NavMain component  with company', () => {
  beforeEach(() => {
    const { result } = renderHook(() => useAppStore())
    act(() => result.current.setDownstreamUser(mockDownstreamUser as any))
    act(() => result.current.setCustomerCompanies(mockCompanies as ICompany[]))
  })

  it(' show reseller company name ', () => {
    const { getAllByText } = render(<SettingMenu />)
    const elem = getAllByText(mockDownstreamUser.companies[0].display_name)[0]
    expect(elem).toBeInTheDocument()
  })
  it('it shouls have have customer company list  ', async () => {
    const { getByText, container } = render(<SettingMenu open />)
    const items = container.querySelectorAll('.ant-dropdown-menu-item')
    const cusDom = getByText(mockCompanies[1].display_name)
    fireEvent.click(cusDom)
    const org = window.localStorage.getItem('currentCompany')
    await waitFor(() => expect(items.length).toEqual(mockCompanies.length + 1))
    expect(org).toEqual(mockCompanies[1].id)
  })

  it('it should remove currentCompany when click reseller company  ', async () => {
    const { container } = render(<SettingMenu open />)
    const items = container.querySelectorAll('.ant-dropdown-menu-item')[0]
    fireEvent.click(items)
    const org = window.localStorage.getItem('currentCompany')
    await waitFor(() => expect(items.textContent).toEqual(mockDownstreamUser.companies[0].display_name))
    expect(org).toBeNull()
  })
})
