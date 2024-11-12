import { renderHook, act } from '@testing-library/react'
import { Company, useCompanyStore } from '../company.store'

describe('useCompanyStore', () => {
  beforeEach(() => {
    const { companies, addCompany } = useCompanyStore.getState()
    companies.splice(0, companies.length)
    addCompany({ key: '1', title: 'Company 1', id: 'IDA92333', shortName: 'comp1', status: 'active' })
    addCompany({ key: '2', title: 'Company 2', id: 'IDNG2333', shortName: 'comp2', status: 'inactive' })
  })

  it('should add a new company', () => {
    const { result } = renderHook(() => useCompanyStore())
    const newCompany: Company = {
      key: '3',
      title: 'New Company',
      id: 'IDNEW333',
      shortName: 'newco',
      status: 'active'
    }

    act(() => {
      result.current.addCompany(newCompany)
    })
    expect(result.current.companies).toContainEqual(newCompany)
  })

  it('should update an existing company record', () => {
    const { result } = renderHook(() => useCompanyStore())
    const updatedCompany: Company = {
      key: '1',
      title: 'Updated Company',
      id: 'IDA92333',
      shortName: 'updated_co',
      status: 'active'
    }
    act(() => {
      result.current.updateCompanyRecord(updatedCompany)
    })
    expect(result.current.companies).toContainEqual(updatedCompany)
  })

  it('should update company status', () => {
    const { result } = renderHook(() => useCompanyStore())
    act(() => {
      result.current.updateCompanyStatus('1', 'inactive')
    })
    const updatedCompany = result.current.companies.find((company) => company.key === '1')
    expect(updatedCompany?.status).toBe('inactive')
  })

  it('should delete a company by ID', () => {
    const { result } = renderHook(() => useCompanyStore())
    act(() => {
      result.current.deleteCompany('IDA92333')
    })
    expect(result.current.companies.find((company) => company.id === 'IDA92333')).toBeUndefined()
  })

  it('should retrieve a company by ID', () => {
    const { result } = renderHook(() => useCompanyStore())
    const company = result.current.getCompanyById('IDA92333')
    expect(company?.title).toBe('Company 1')
  })
})
