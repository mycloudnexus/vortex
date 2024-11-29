export interface ICompany {
  name: string
  id: string
  display_name: string
  metadata: MetaData
  branding: Branding
}

interface MetaData {
  loginType: string
  status: string
  type: string
}

interface Branding {
  logo_url: string
  colors: Colors
}

interface Colors {
  primary: string
  page_background: string
}

export interface CreateOrganizationRequestBody {
  name: string
  metadata: MetaData
  display_name: string
}

export interface CreateOrganizationResponse {
  code: number
  message: string
  data: ICompany
}

export interface IOrganization {
  data: ICompany[]
  page: number
  size: number
  total: number
}

export interface RequestResponse<Data> {
  code: number
  message: string
  data: Data
}
