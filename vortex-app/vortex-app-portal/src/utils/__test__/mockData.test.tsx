import { getConnectionRequestBody, getConnectionResponse, getOrganizationResponse } from '../mockData'

describe('Mock data test', () => {
  it('should call the organization response', () => {
    expect(getOrganizationResponse().code).toBe(200)
  })
  it('should call the connection response', () => {
    expect(getConnectionResponse().code).toBe(200)
  })
  it('should call the connection request body', () => {
    expect(getConnectionRequestBody().strategy).toBe('samlp')
  })
})
