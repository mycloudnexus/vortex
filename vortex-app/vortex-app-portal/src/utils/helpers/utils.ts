import { CreateOrganizationResponse, ICompany } from '@/services/types'

export const updateData = (obj: ICompany[], res: CreateOrganizationResponse): ICompany[] => {
  return obj.map((val: ICompany) => (val.id === res.data.id ? { ...res.data } : val))
}
