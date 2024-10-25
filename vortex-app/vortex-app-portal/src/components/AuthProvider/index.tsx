import * as React from 'react'
import { Auth0Provider } from '@auth0/auth0-react'
import { AUTH0_PROVIDER_ATTRIBS } from '@/constant'

interface AuthProviderProps {
  defaultReturnTo?: string
  children: React.ReactElement
}

export const AuthProvider = ({ children }: AuthProviderProps) => {
  return (
    <Auth0Provider
      domain={AUTH0_PROVIDER_ATTRIBS.domain}
      clientId={AUTH0_PROVIDER_ATTRIBS.clientId}
      authorizationParams={{
        redirect_uri: window.location.origin
      }}
    >
      {children}
    </Auth0Provider>
  )
}
