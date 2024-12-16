export interface ICompany {
  name: string
  id: string
  display_name: string
  connection: Connection
  metadata: MetaData
  branding: Branding
}

interface Connection {
  name: string
  strategy: string
  display_name: string
  options: {
    additionalProp1: Object
    additionalProp2: Object
    additionalProp3: Object
  }
  id: string
  enabled_clients: string[]
  provisioning_ticket_url: string
  metadata: {
    additionalProp1: string
    additionalProp2: string
    additionalProp3: string
  }
  realms: string
}

interface MetaData {
  status: string
  strategy: string
  connectionId: string
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

export interface UpdateOrganizationRequestBody {
  request_body: { display_name?: string; status?: string }
  id: string
}

interface Samlp {
  signingCert: string
  signSAMLRequest: boolean
  signatureAlgorithm: string
  digestAlgorithm: string
  fieldsMap: Object
  signInEndpoint: string
  signOutEndpoint: string
  debug: boolean
}

export interface AddConnectionRequestBody {
  strategy: string
  saml: Samlp
}

export interface AddConnectionResponse {
  name: string
  strategy: string
  options: Options
  id: string
  enabled_clients: string[]
  provisioning_ticket_url: string
  realms: string[]
}

export interface Options {
  signInEndpoint: string
  signingCert: string
  debug: boolean
  signOutEndpoint: string
  signSAMLRequest: boolean
  digestAlgorithm: string
  signatureAlgorithm: string
  fieldsMap: FieldsMap
  expires: Date
  subject: Subject
  thumbprints: string[]
  cert: string
}

export interface FieldsMap {}

export interface Subject {
  commonName: string
}
