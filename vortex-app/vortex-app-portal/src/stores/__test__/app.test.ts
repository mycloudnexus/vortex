import { act, renderHook } from '@testing-library/react'
import { useAppStore } from '../app.store'
import type { User, Role, CustomerUser } from '../type'
import type { ICompany } from '@/services/types'

describe('useAppStore test', () => {
  it('update appstore variables', () => {
    const logoUrl = 'test.com'
    const company = { name: 'company', id: '123' }
    const downstreamUser = { name: 'user' } as User
    const roleList = {} as Role
    const customerUser = { name: 'customername' } as CustomerUser
    const customerCompanies = [{ name: 'customerCompanies' }] as ICompany[]
    const { result } = renderHook(() => useAppStore())
    act(() => result.current.setAppLogo(logoUrl))
    act(() => result.current.setCurrentCompany(company))
    act(() => result.current.setDownstreamUser(downstreamUser))
    act(() => result.current.setRoleList(roleList))
    act(() => result.current.setCustomerUser(customerUser))
    act(() => result.current.setCustomerCompanies(customerCompanies))
    expect(result.current.appLogo).toEqual(logoUrl)
    expect(result.current.currentCompany).toEqual(company)
    expect(result.current.downstreamUser).toEqual(downstreamUser)
    expect(result.current.roleList).toEqual(roleList)
    expect(result.current.customerUser).toEqual(customerUser)
    expect(result.current.customerCompanies).toEqual(customerCompanies)
  })
})
