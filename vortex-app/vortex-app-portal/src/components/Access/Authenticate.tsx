import { Skeleton } from 'antd'
import { useEffect, ReactNode, useCallback, useState } from 'react'
import { useAuth0 } from '@auth0/auth0-react'
import { useNavigate } from 'react-router-dom'
import { getOrg, storeToken } from '@/utils/helpers/token'
import { useAppStore } from '@/stores/app.store'
import type { AuthUser } from '@/stores/type'

interface AuthenticateProps {
  children: ReactNode
}

const Authenticate = ({ children }: AuthenticateProps) => {
  const [haveToken, setHaveToken] = useState(false)

  const { isLoading, isAuthenticated, user, getAccessTokenSilently, error } = useAuth0()

  const { currentAuth0User, setCurrentAuth0User, setuserType } = useAppStore()
  const navigate = useNavigate()

  useEffect(() => {
    if (window.localStorage.getItem('org')) {
      setuserType('customer')
    }
  }, [])

  const saveToken = useCallback(async () => {
    const res = await getAccessTokenSilently()
    window.portalToken = res
    storeToken(res)
    setHaveToken(true)
  }, [getAccessTokenSilently, setHaveToken])

  useEffect(() => {
    if (!isAuthenticated && !isLoading) {
      const org = getOrg()
      const loginUrl = org ? `${org}/login` : 'login'
      navigate(loginUrl)
      setHaveToken(false)
    }
    if (isAuthenticated) {
      saveToken()
    }
    if (isAuthenticated && !currentAuth0User && user) {
      setCurrentAuth0User(user as AuthUser)
    }
  }, [isAuthenticated, isLoading, user, saveToken])

  if (error) {
    return <div>Oops... {error.message}</div>
  }
  if (isLoading || !haveToken) {
    return <Skeleton />
  }
  return children
}

export default Authenticate
