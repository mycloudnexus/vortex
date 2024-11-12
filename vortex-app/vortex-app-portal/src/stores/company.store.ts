import { create } from 'zustand'

export interface Company {
  key: string
  title: string
  id: string
  shortName: string
  status: 'active' | 'inactive'
}

interface CompanyStore {
  companies: Company[]
  addCompany: (company: Company) => void
  updateCompanyRecord: (company: Company) => void
  updateCompanyStatus: (key: string, status: 'active' | 'inactive') => void
  deleteCompany: (id: string) => void
  getCompanyById: (id: string) => Company | undefined
}

export const useCompanyStore = create<CompanyStore>((set, get) => ({
  companies: [
    {
      key: '1',
      title: 'Start In Tech Singapore',
      id: 'IDA92333',
      shortName: 'start_in_tech',
      status: 'active'
    },
    {
      key: '2',
      title: 'MediaMango Tech LTD Australia',
      id: 'IDNG2333',
      shortName: 'mediamango',
      status: 'inactive'
    },
    {
      key: '3',
      title: 'Reseller customer company C',
      id: 'IDEO2333',
      shortName: 'suspendisse',
      status: 'active'
    }
  ],
  addCompany: (company) => set((state) => ({ companies: [...state.companies, company] })),
  updateCompanyStatus: (key, status) =>
    set((state) => ({
      companies: state.companies.map((company) => (company.key === key ? { ...company, status } : company))
    })),
  updateCompanyRecord: (updatedCompany) =>
    set((state) => ({
      companies: state.companies.map((company) =>
        company.key === updatedCompany.key ? { ...updatedCompany } : company
      )
    })),
  deleteCompany: (id) =>
    set((state) => ({
      companies: state.companies.filter((company) => company.id !== id)
    })),
  getCompanyById: (id) => {
    const company = get().companies.find((company) => company.id === id)
    return company
  }
}))
