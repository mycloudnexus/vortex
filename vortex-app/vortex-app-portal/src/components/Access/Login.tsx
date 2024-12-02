/* eslint-disable @typescript-eslint/no-unused-vars */

import React, { useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useAuth0 } from '@auth0/auth0-react'
import { Spin } from 'antd'
import { ENV } from '@/constant'
import { storeOrg } from '@/utils/helpers/token'
import { useAppStore } from '@/stores/app.store'
import type { AuthUser } from '@/stores/type'

const Login = () => {
  const { isLoading, isAuthenticated, loginWithRedirect, user } = useAuth0()
  const navigate = useNavigate()
  const { organization = ENV.RESELLER_AUTH0_MGMT_ORG_ID } = useParams()
  const { setCurrentAuth0User } = useAppStore()

  useEffect(() => {
    storeOrg(organization!)
  }, [organization])

  useEffect(() => {
    if (!isAuthenticated && !isLoading) {
      loginWithRedirect({
        authorizationParams: {
          // organization,
          redirect_uri: window.location.origin
        }
      })
    }
  }, [isAuthenticated, isLoading, loginWithRedirect, organization])

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
