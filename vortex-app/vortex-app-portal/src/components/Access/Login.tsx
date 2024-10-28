/* eslint-disable @typescript-eslint/no-unused-vars */

import React, { useCallback, useEffect, ReactNode } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useAuth0 } from '@auth0/auth0-react'
import { Spin } from 'antd'
import { useAppStore } from '@/stores/app.store'
import type { AuthUser } from '@/stores/app.store'

// 'org_doRtBm1mKsGlUw3W' //usename password
//  'org_rKgjdXyGVS37FUuo'//consoleconnect

const Login = () => {
  // const organization = window.localStorage.getItem('org') || 'org_rKgjdXyGVS37FUuo'
  const { getAccessTokenSilently, isLoading, isAuthenticated, loginWithRedirect, user } = useAuth0()
  const navigate = useNavigate()
  const { organization = 'org_rKgjdXyGVS37FUuo' } = useParams()
  console.log('-organizationorganization', organization)
  const { setCurrentUser } = useAppStore()

  const saveToken = useCallback(async () => {
    const res = await getAccessTokenSilently()
    window.localStorage.setItem('token', res)
  }, [getAccessTokenSilently])

  useEffect(() => {
    if (!isAuthenticated && !isLoading) {
      loginWithRedirect({
        authorizationParams: {
          organization,
          redirect_uri: window.location.origin
        }
      })
    }
    if (isAuthenticated) {
      saveToken()
    }
  }, [saveToken, isAuthenticated, isLoading, loginWithRedirect, organization])

  useEffect(() => {
    if (!user) return
    setCurrentUser(user as AuthUser)
    navigate('/')
  }, [user, setCurrentUser])

  if (isLoading || !isAuthenticated) {
    return <Spin />
  }

  return <></>
}

export default Login
