import React from 'react'
import { Auth0Provider } from '@auth0/auth0-react'
import { ENV, CUSTOMER_AUTH0, RESELLER_AUTH0 } from '@/constant'
import { useAppStore } from '@/stores/app.store'

interface AuthProviderProps {
  defaultReturnTo?: string
  children: React.ReactElement
}
window.portalConfig = ENV

export const AuthProvider = ({ children }: AuthProviderProps) => {
  const { userType } = useAppStore()
  if (!userType) {
    return null
  }
  if (userType === 'reseller') {
    return (
      <Auth0Provider key={userType} {...RESELLER_AUTH0}>
        {children}
      </Auth0Provider>
    )
  }
  if (userType === 'customer') {
    return (
      <Auth0Provider key={userType} {...CUSTOMER_AUTH0}>
        {children}
      </Auth0Provider>
    )
  }
}
