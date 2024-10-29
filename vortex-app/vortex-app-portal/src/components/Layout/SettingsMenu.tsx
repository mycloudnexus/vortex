import useDeviceDetect from '@/hooks/useDeviceDetect'
import { Avatar, Dropdown, Flex } from 'antd'
import Text from '../Text'
import { useAppStore } from '@/stores/app.store'
import { DownOutlined, MenuOutlined } from '@ant-design/icons'
import * as styles from './index.module.scss'
import { useState } from 'react'
import MobileDrawer from './MobileDrawer'
import { useAuth0 } from '@auth0/auth0-react'

const SettingsMenu = () => {
  const { isMobile } = useDeviceDetect()
  const { currentCompany, currentAuth0User, setCurrentAuth0User } = useAppStore()
  const [openDrawer, setOpenDrawer] = useState(false)
  const { logout } = useAuth0()
  const items = [
    {
      label: 'po',
      key: '0',
      onClick: () => {}
    },
    {
      label: 'ping',
      key: '1',
      onClick: () => {}
    }
  ]
  const dropdownRender = () => {
    return (
      <div className={styles.avatarMenu}>
        <Flex justify='space-between'>
          <Flex vertical gap={10}>
            <div className={styles.userName}>{currentAuth0User?.nickname}</div>
            <div className={styles.avatarItem}>Personal details</div>
          </Flex>
        </Flex>
        <div className={styles.divider} />
        <div
          className={styles.avatarItem}
          onClick={() => {
            setCurrentAuth0User(null)
            window.localStorage.removeItem('token')
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
          <Flex gap={8}>
            <Text.LightMedium color='rgba(255, 255, 255, 0.50)'>Viewing as</Text.LightMedium>
            <Dropdown
              menu={{ items }}
              getPopupContainer={() => document.getElementById('nav') as HTMLDivElement}
              overlayClassName={styles.settingDropdown}
            >
              <Flex gap={4}>
                <Text.LightMedium color='#fff'>{currentCompany?.name}</Text.LightMedium>
                <DownOutlined style={{ color: '#fff', fontSize: 10 }} />
              </Flex>
            </Dropdown>
          </Flex>
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
