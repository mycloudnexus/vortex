import React, { useEffect, ReactNode, useCallback } from 'react'
import { useAuth0 } from '@auth0/auth0-react'
import { useNavigate } from 'react-router-dom'
import { filter, get } from 'lodash'
import { Spin } from 'antd'
import { ENV } from '@/constant'
import { useGetUserAuthDetail, useGetUserRole } from '@/hooks/user'
import { useAppStore } from '@/stores/app.store'
import type { AuthUser } from '@/stores/type'
import { getOrg, storeToken } from '@/utils/helpers/token'

interface AuthenticateProps {
  children: ReactNode
}

const Authenticate = ({ children }: AuthenticateProps) => {
  const { isLoading, isAuthenticated, user, getAccessTokenSilently, error } = useAuth0()
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
    const accessRoles = filter(roleList, (r) => roleIds.includes(r.id) || r.systemDefault)
    const allUserDetail = { ...userDetail, accessRoles }
    window.portalAccessRoles = roleList
    window.portalLoggedInUser = allUserDetail

    setRoleList(roleList)
    setUser(allUserDetail)
  }, [userData, roleData])

  useEffect(() => {
    if (!isAuthenticated && !isLoading) {
      const org = getOrg() ?? ENV.RESELLER_AUTH0_MGMT_ORG_ID
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
  if (error) {
    return <div>Oops... {error.message}</div>
  }

  return children
}

export default Authenticate
