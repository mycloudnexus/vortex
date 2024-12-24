import { useEffect, useMemo } from 'react'
import { useNavigate, useParams, useLocation } from 'react-router-dom'
import { useAuth0 } from '@auth0/auth0-react'
import { Flex, Button, Skeleton } from 'antd'
import { storeOrg } from '@/utils/helpers/token'
import { useAppStore } from '@/stores/app.store'
import { Logo } from './Icon'
import type { AuthUser } from '@/stores/type'
import * as styles from './index.module.scss'

const Login = () => {
  const { loginWithRedirect, user, error } = useAuth0()
  const navigate = useNavigate()
  const { organization } = useParams()
  const { setCurrentAuth0User, setuserType } = useAppStore()
  const { search } = useLocation()
  const searchParams = new URLSearchParams(search?.slice(1))

  useEffect(() => {
    if (organization) {
      storeOrg(organization)
      setuserType('customer')
    } else {
      setuserType('reseller')
    }
  }, [organization])

  const authorizationParams = useMemo(() => {
    const url = window.location.origin
    const obj = { redirect_uri: organization ? url : `${url}/login` }
    return organization ? { ...obj, organization } : obj
  }, [organization])

  useEffect(() => {
    if (!user) return
    setCurrentAuth0User(user as AuthUser)
    navigate('/')
  }, [user, setCurrentAuth0User])

  if (error) {
    return <div>{error.message}</div>
  }

  if (searchParams.get('code')) {
    return <Skeleton />
  }

  return (
    <div className={styles.container}>
      <div className={styles.bgContainer}></div>
      <Flex vertical align='center' justify='center' className={styles.flexContainer}>
        <Flex align='center'>
          <Logo />
          <b style={{ marginLeft: 4, fontSize: 20, fontWeight: 700, color: '#292929' }}>goPartnerConnect</b>
        </Flex>
        <h1 style={{ marginTop: 24 }}>ACCOUNT LOGIN</h1>
        <p>Log in to goPartnerConnect.</p>
        <Button
          type='primary'
          onClick={() => {
            loginWithRedirect({ authorizationParams })
          }}
        >
          Log in
        </Button>
      </Flex>
    </div>
  )
}

export default Login
