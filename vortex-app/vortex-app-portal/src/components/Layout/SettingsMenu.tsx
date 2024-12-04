import { Avatar, Dropdown, Flex } from 'antd'
import { DownOutlined, MenuOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import useDeviceDetect from '@/hooks/useDeviceDetect'
import { useAuth0 } from '@auth0/auth0-react'
import { useAppStore } from '@/stores/app.store'
import { clearToken } from '@/utils/helpers/token'
import { DefaultCompanyLogo } from './Icon'

import Text from '../Text'

import { useEffect, useMemo, useState } from 'react'
import MobileDrawer from './MobileDrawer'
import type { User, CustomerUser } from '@/stores/type'
import * as styles from './index.module.scss'

const SettingsMenu = () => {
  const [user, setUser] = useState<User | CustomerUser | null>()
  const [openDrawer, setOpenDrawer] = useState(false)
  const { isMobile } = useDeviceDetect()
  const navigate = useNavigate()
  const {
    currentCompany,
    customerCompanies,
    currentAuth0User,
    downstreamUser,
    customerUser,
    userType,
    mainColor,
    setCurrentCompany,
    setCurrentAuth0User
  } = useAppStore()
  const { logout } = useAuth0()

  useEffect(() => {
    if (!userType) return
    if (!downstreamUser && !customerUser) return
    if (userType === 'customer') {
      setUser(customerUser)
    } else {
      setUser(downstreamUser)
    }
  }, [downstreamUser, customerUser, userType])
  const downstreamCompany = useMemo(() => {
    const n = downstreamUser?.companies[0]?.name ?? ''
    return {
      id: downstreamUser?.companies[0]?.id ?? '',
      name: n,
      display_name: n
    }
  }, [downstreamUser])

  const companyItems = useMemo(() => {
    return [downstreamCompany, ...customerCompanies].map((i, n) => {
      return {
        label: (
          <div className={`${styles.companyItem} ${n === 0 ? styles.reseller : ''}`}>
            <div style={{ color: n === 0 ? mainColor : 'rgba(0, 0, 0, 0.45)', marginTop: 4 }}>
              <DefaultCompanyLogo />
            </div>

            {i.display_name}
          </div>
        ),
        key: i.id,
        onClick: () => {
          setCurrentCompany({
            id: i.id,
            name: i.display_name
          })
          if (n !== 0) {
            window.localStorage.setItem('currentCompany', i.id ?? '')
          } else {
            window.localStorage.removeItem('currentCompany')
          }
          navigate('/port')
        }
      }
    })
  }, [customerCompanies, downstreamCompany, setCurrentCompany])

  const isCustomer = useMemo(() => {
    return userType === 'customer'
  }, [userType])
  console.log('currentCompany', currentCompany)

  useEffect(() => {
    const customerCompany = {
      id: customerUser?.userInfo?.organization?.id ?? '',
      name: customerUser?.userInfo?.organization?.display_name ?? ''
    }
    setCurrentCompany(isCustomer ? customerCompany : downstreamCompany)
  }, [downstreamCompany, customerUser, customerUser, setCurrentCompany, isCustomer])
  useEffect(() => {
    if (isCustomer || (!isCustomer && downstreamCompany.id === currentCompany?.id)) {
      window.localStorage.removeItem('currentCompany')
    }
  }, [isCustomer, currentCompany, downstreamCompany])

  const dropdownRender = () => {
    return (
      <div className={styles.avatarMenu}>
        <Flex justify='space-between'>
          <Flex vertical gap={10}>
            <div className={styles.userName}>{user?.name}</div>
            <div className={styles.avatarItem}>Personal details</div>
          </Flex>
        </Flex>
        <div className={styles.divider} />
        <div
          className={styles.avatarItem}
          onClick={() => {
            setCurrentAuth0User(null)
            clearToken()
            logout({
              logoutParams: {
                returnTo: window.location.origin
              }
            })
          }}
        >
          Logout
        </div>
      </div>
    )
  }
  return (
    <Flex align='center'>
      {openDrawer && <MobileDrawer open={openDrawer} onClose={() => setOpenDrawer(false)} />}
      {isMobile ? (
        <div className={styles.menuMobile}>
          <MenuOutlined style={{ color: '#fff' }} onClick={() => setOpenDrawer(true)} />
        </div>
      ) : (
        <Flex align='center' id='nav' gap={24} justify='flex-end' style={{ color: 'red' }}>
          {currentCompany?.name && (
            <Flex gap={8}>
              <Text.LightMedium color='rgba(255, 255, 255, 0.50)'>Viewing as</Text.LightMedium>
              <Dropdown
                menu={{ items: isCustomer ? [] : companyItems }}
                getPopupContainer={() => document.getElementById('nav') as HTMLDivElement}
                overlayClassName={styles.settingDropdown}
              >
                <Flex gap={4}>
                  <Text.LightMedium color='#fff'>{currentCompany?.name}</Text.LightMedium>
                  {!isCustomer && <DownOutlined style={{ color: '#fff', fontSize: 10 }} />}
                </Flex>
              </Dropdown>
            </Flex>
          )}

          <Dropdown
            menu={{ items: [] }}
            getPopupContainer={() => document.getElementById('nav') as HTMLDivElement}
            dropdownRender={dropdownRender}
          >
            {currentAuth0User ? (
              <Avatar size='large' src={<img src={currentAuth0User?.picture} alt='avatar' />} />
            ) : (
              <></>
            )}
          </Dropdown>
        </Flex>
      )}
    </Flex>
  )
}

export default SettingsMenu
