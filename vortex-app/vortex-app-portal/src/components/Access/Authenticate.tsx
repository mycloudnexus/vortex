import { Spin } from 'antd'
import React, { useEffect, ReactNode, useCallback } from 'react'
import { useAuth0 } from '@auth0/auth0-react'
import { useNavigate } from 'react-router-dom'
import { filter, get } from 'lodash'
import { useGetUserAuthDetail, useGetUserRole } from '@/hooks/user'
import { getOrg, storeToken } from '@/utils/helpers/token'
import { ENV } from '@/constant'
import { useAppStore } from '@/stores/app.store'
import type { AuthUser } from '@/stores/type'

interface AuthenticateProps {
  children: ReactNode
}

const Authenticate = ({ children }: AuthenticateProps) => {
  const { isLoading, isAuthenticated, user, getAccessTokenSilently } = useAuth0()
  const { currentAuth0User, setCurrentAuth0User, setUser, setRoleList } = useAppStore()
  const navigate = useNavigate()

  const { data: userData } = useGetUserAuthDetail()
  const { data: roleData } = useGetUserRole()

  const saveToken = useCallback(async () => {
    const res = await getAccessTokenSilently()
    storeToken(res)
  }, [getAccessTokenSilently])

  useEffect(() => {
    const userDetail = userData?.data
    const roleList = roleData?.data
    if (!userDetail) return
    const companyId = get(userDetail, 'companies[0].id', '')
    const roleIds = get(userDetail, ['linkUserCompany', companyId, 'roleIds'], [])
    const accessRole = filter(roleList, (r) => roleIds.includes(r.id) || r.systemDefault)
    window.portalAccessRoles = roleList
    window.portalLoggedInUser = userDetail
    setRoleList(roleList)
    setUser({ ...userDetail, accessRole })
  }, [userData, roleData])

  useEffect(() => {
    if (!isAuthenticated && !isLoading) {
      const org = getOrg() || ENV.AUTH0_MGMT_ORG_ID
      navigate(`${org}/login`)
    }
    if (isAuthenticated) {
      saveToken()
    }
    if (isAuthenticated && !currentAuth0User && user) {
      setCurrentAuth0User(user as AuthUser)
    }
  }, [isAuthenticated, isLoading, user, saveToken])

  if (isLoading || !isAuthenticated) {
    return <Spin />
  }

  return children
}

export default Authenticate
