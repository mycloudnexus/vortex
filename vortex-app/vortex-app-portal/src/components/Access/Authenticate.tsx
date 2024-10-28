import React, { useEffect, ReactNode } from 'react'
import { useAuth0 } from '@auth0/auth0-react'
import { useNavigate } from 'react-router-dom'
import { Spin } from 'antd'
import { useAppStore } from '@/stores/app.store'
import type { AuthUser } from '@/stores/app.store'

// 'org_doRtBm1mKsGlUw3W' //usename password
//  'org_rKgjdXyGVS37FUuo'//consoleconnect

interface AuthenticateProps {
  children: ReactNode
}

const Authenticate = ({ children }: AuthenticateProps) => {
  const { isLoading, isAuthenticated, user } = useAuth0()
  const { currentUser, setCurrentUser } = useAppStore()
  const navigate = useNavigate()

  useEffect(() => {
    if (!isAuthenticated && !isLoading) {
      const { org_id: org = 'org_rKgjdXyGVS37FUuo' } = currentUser ?? {}
      navigate(`${org}/login`)
    }
    if (isAuthenticated && !currentUser && user) {
      setCurrentUser(user as AuthUser)
    }
  }, [isAuthenticated, isLoading, user])

  if (isLoading || !isAuthenticated) {
    return <Spin />
  }

  return children
}

export default Authenticate
