import { render, renderHook, act } from '@testing-library/react'
import { useAppStore } from '@/stores/app.store'

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

describe('NavMain component ', () => {
  it(`it shouldn't show reseller company name `, () => {
    const { queryByText } = render(<SettingMenu />)
    const elem = queryByText(/Viewing as/i)
    expect(elem).not.toBeInTheDocument()
  })
  it(' show reseller company name ', () => {
    const { result } = renderHook(() => useAppStore())
    act(() => result.current.setDownstreamUser(mockDownstreamUser as any))
    const { getByText } = render(<SettingMenu />)
    const elem = getByText(mockDownstreamUser.companies[0].display_name)
    expect(elem).toBeInTheDocument()
  })
})
