/* eslint-disable @typescript-eslint/no-unused-vars */

import React, { useCallback, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useAuth0 } from '@auth0/auth0-react'
import { Spin } from 'antd'
import { ENV } from '@/constant'
import { storeOrg, storeToken } from '@/utils/helpers/token'
import { useAppStore } from '@/stores/app.store'
import type { AuthUser } from '@/stores/app.store'

const Login = () => {
  const { getAccessTokenSilently, isLoading, isAuthenticated, loginWithRedirect, user } = useAuth0()
  const navigate = useNavigate()
  const { organization = ENV.AUTH0_MGMT_ORG_ID } = useParams()
  const { setCurrentAuth0User } = useAppStore()

  const saveToken = useCallback(async () => {
    const res = await getAccessTokenSilently()
    storeToken(res)
  }, [getAccessTokenSilently])

  useEffect(() => {
    storeOrg(organization!)
  }, [organization])

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
    setCurrentAuth0User(user as AuthUser)
    navigate('/')
  }, [user, setCurrentAuth0User])

  if (isLoading || !isAuthenticated) {
    return <Spin />
  }

  return <></>
}

export default Login
