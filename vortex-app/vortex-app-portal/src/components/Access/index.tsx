import * as React from 'react'
import { Auth0Provider } from '@auth0/auth0-react'
import { ENV } from '@/constant'

interface AuthProviderProps {
  defaultReturnTo?: string
  children: React.ReactElement
}

export const AuthProvider = ({ children }: AuthProviderProps) => {
  return (
    <Auth0Provider
      domain={ENV.AUTH0_DOMAIN!}
      clientId={ENV.AUTH0_CLIENT_ID!}
      authorizationParams={{
        redirect_uri: window.location.origin,
        audience: ENV.AUTH0_AUDIENCE
      }}
    >
      {children}
    </Auth0Provider>
  )
}
