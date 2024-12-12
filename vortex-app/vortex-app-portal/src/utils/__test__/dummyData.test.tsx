import { connectionRequestBody, connectionResponse, organizationResponse } from '../dummyData'

describe('test dummy data', () => {
  it('should define the organization response', () => {
    expect(organizationResponse).toBeDefined()
  })
  it('should define the connect response', () => {
    expect(connectionResponse).toBeDefined()
  })
  it('should define the connection request body', () => {
    expect(connectionRequestBody).toBeDefined()
  })
})
