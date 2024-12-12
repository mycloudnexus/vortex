import { getOrganizationResponse } from '../mockData'

describe('Mock data test', () => {
  it('should call the organization response', () => {
    expect(getOrganizationResponse().code).toBe(200)
  })
})
