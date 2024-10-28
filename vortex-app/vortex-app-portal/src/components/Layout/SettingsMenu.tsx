import useDeviceDetect from '@/hooks/useDeviceDetect'
import { Avatar, Dropdown, Flex } from 'antd'
import Text from '../Text'
import { useAppStore } from '@/stores/app.store'
import { DownOutlined, MenuOutlined } from '@ant-design/icons'
import * as styles from './index.module.scss'
import { useState } from 'react'
import MobileDrawer from './MobileDrawer'

const SettingsMenu = () => {
  const { isMobile } = useDeviceDetect()
  const { currentCompany, currentUser } = useAppStore()
  const [openDrawer, setOpenDrawer] = useState(false)
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
            <div className={styles.userName}>{currentUser?.name}</div>
            <div className={styles.avatarItem}>Personal details</div>
          </Flex>
          <Avatar size='large'>TP</Avatar>
        </Flex>
        <div className={styles.divider} />
        <div className={styles.avatarItem}>Logout</div>
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
        <Flex align='center' id='nav' gap={24} justify='flex-end'>
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
            <Avatar size='large'>TP</Avatar>
          </Dropdown>
        </Flex>
      )}
    </Flex>
  )
}

export default SettingsMenu
