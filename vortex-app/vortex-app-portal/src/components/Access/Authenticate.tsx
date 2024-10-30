import { Spin } from 'antd'
import React, { useEffect, ReactNode } from 'react'
import { useAuth0 } from '@auth0/auth0-react'
import { useNavigate } from 'react-router-dom'
import { filter, get } from 'lodash'
import { useGetUserAuthDetail, useGetUserRole } from '@/hooks/user'
import { ENV } from '@/constant'
import { useAppStore } from '@/stores/app.store'
import type { AuthUser } from '@/stores/type'

interface AuthenticateProps {
  children: ReactNode
}

const Authenticate = ({ children }: AuthenticateProps) => {
  const { isLoading, isAuthenticated, user } = useAuth0()
  const { currentAuth0User, setCurrentAuth0User, setUser, setRoleList } = useAppStore()
  const navigate = useNavigate()

  const { data: userData } = useGetUserAuthDetail()
  const { data: roleData } = useGetUserRole()

  useEffect(() => {
    const userDetail = userData?.data
    const roleList = roleData?.data
    if (!userDetail) return
    const companyId = get(userDetail, 'companies[0].id', '')
    const roleIds = get(userDetail, ['linkUserCompany', companyId, 'roleIds'], [])
    const accessRole = filter(roleList, (r) => roleIds.includes(r.id) || r.systemDefault)
    setRoleList(roleList)
    setUser({ ...userDetail, accessRole })
  }, [userData, roleData])

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
