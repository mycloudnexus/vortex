import React, { useCallback, useEffect, ReactNode } from 'react'
import { useAuth0 } from '@auth0/auth0-react'
import { Spin } from 'antd'
import { useAppStore } from '@/stores/app.store'
import type { AuthUser } from '@/stores/app.store'

// 'org_doRtBm1mKsGlUw3W' //usename password
//  'org_rKgjdXyGVS37FUuo'//consoleconnect

interface AuthenticateProps {
  children: ReactNode
}

const Authenticate = ({ children }: AuthenticateProps) => {
  const organization = window.localStorage.getItem('org') || 'org_rKgjdXyGVS37FUuo'

  const { getAccessTokenSilently, isLoading, isAuthenticated, loginWithRedirect, user } = useAuth0()
  const { setCurrentUser } = useAppStore()
  const saveToken = useCallback(async () => {
    const res = await getAccessTokenSilently()
    window.localStorage.setItem('token', res)
  }, [getAccessTokenSilently])

  useEffect(() => {
    if (!isAuthenticated && !isLoading) {
      loginWithRedirect({
        authorizationParams: {
          organization
        }
      })
    } else if (isAuthenticated) {
      saveToken()
    }
  }, [saveToken, isAuthenticated, isLoading, loginWithRedirect, user])

  useEffect(() => {
    if (!user) return
    setCurrentUser(user as AuthUser)
  }, [user, setCurrentUser])

  if (isLoading || !isAuthenticated) {
    return <Spin />
  }

  return children
}

export default Authenticate
