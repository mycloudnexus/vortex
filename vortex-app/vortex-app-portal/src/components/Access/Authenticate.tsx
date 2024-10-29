import { Spin } from 'antd'
import React, { useEffect, ReactNode } from 'react'
import { useAuth0 } from '@auth0/auth0-react'
import { useNavigate } from 'react-router-dom'

import { ENV } from '@/constant'
import { useAppStore } from '@/stores/app.store'

import type { AuthUser } from '@/stores/app.store'

interface AuthenticateProps {
  children: ReactNode
}

const Authenticate = ({ children }: AuthenticateProps) => {
  const { isLoading, isAuthenticated, user } = useAuth0()
  const { currentAuth0User, setCurrentAuth0User } = useAppStore()
  const navigate = useNavigate()

  // TODO replace with real username
  // const { data } = useGetUserDetail('lyang2')

  // useEffect(() => {
  //   const user = data?.data
  //   if (!user) return
  //   setUser(user)
  // }, [data])

  useEffect(() => {
    if (!isAuthenticated && !isLoading) {
      const { org_id: org = ENV.AUTH0_MGMT_ORG_ID } = currentAuth0User ?? {}
      navigate(`${org}/login`)
    }
    if (isAuthenticated && !currentAuth0User && user) {
      setCurrentAuth0User(user as AuthUser)
    }
  }, [isAuthenticated, isLoading, user])

  if (isLoading || !isAuthenticated) {
    return <Spin />
  }

  return children
}

export default Authenticate
