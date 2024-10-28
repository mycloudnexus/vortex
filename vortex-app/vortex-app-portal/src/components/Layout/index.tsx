import { Suspense, useEffect, useState } from 'react'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import Headroom from 'react-headroom'
import NavMain from './NavMain'
import * as styles from './index.module.scss'
import Sider from 'antd/es/layout/Sider'
import { Flex, Menu } from 'antd'
import NetIcon from '@/assets/icon/network.svg'
import { useAppStore } from '@/stores/app.store'
import { DoubleLeftOutlined, DoubleRightOutlined, DownOutlined, RightOutlined } from '@ant-design/icons'
import { useBoolean } from 'usehooks-ts'
import Text from '../Text'
import { ReactComponent as DashboardIcon } from '@/assets/icon/dashboard.svg'
import { ReactComponent as DCIcon } from '@/assets/icon/dcport.svg'
import { ReactComponent as CRIcon } from '@/assets/icon/cloudrouter.svg'
import { ReactComponent as L2Icon } from '@/assets/icon//l2.svg'
import { ReactComponent as L3Icon } from '@/assets/icon/l3.svg'
import { ReactComponent as SettingIcon } from '@/assets/icon/setting.svg'
import { styled } from 'styled-components'
import useDeviceDetect from '@/hooks/useDeviceDetect'
import MainMenuMobileDrawer from './MainMenuMobileDrawer'
import Authenticate from '../Access/Authenticate'

const Layout = () => {
  const { mainColor } = useAppStore()
  const SliderCustom = styled(Sider)`
    .ant-menu-submenu-selected {
      svg {
        path {
          stroke: ${mainColor};
        }
      }
    }
    .ant-menu-item-selected {
      svg {
        path {
          fill: ${mainColor};
          stroke: ${mainColor};
        }
        circle {
          stroke: ${mainColor};
        }
      }
    }
  `
  const { isMobile } = useDeviceDetect()
  const [activeKeys, setActiveKeys] = useState(['1'])
  const { value: collapsed, setValue: setCollapsed, setTrue: trueCollapse, setFalse: falseCollapse } = useBoolean(false)
  const { value: mainMobileDrawer, toggle: toggleDrawer, setFalse: falseDrawer } = useBoolean(false)
  const location = useLocation()
  const [openKeys, setOpenKeys] = useState<string[]>()
  const navigate = useNavigate()
  useEffect(() => {
    if (!isMobile) {
      falseDrawer()
    }
  }, [isMobile])

  useEffect(() => {
    if (location.pathname === '/') {
      setActiveKeys(['1'])
      return
    }
    for (const m of items) {
      if (m?.regex?.test(location.pathname)) {
        setActiveKeys([m.key])
      }
      if (m.children) {
        for (const c of m.children) {
          if ((c as any).regex?.test(location.pathname)) {
            setActiveKeys([m.key, c.key])
          }
        }
      }
    }
  }, [location])

  const items = [
    {
      key: '1',
      icon: <DashboardIcon />,
      label: 'Dashboard',
      regex: /^\/dashboard(\/.*)?$/,
      onClick: () => {
        navigate('/')
      }
    },
    {
      key: '2',
      icon: <DCIcon />,
      label: 'DC Ports',
      children: [{ key: '2-1', label: 'View all' }]
    },
    {
      key: '3',
      icon: <L2Icon />,
      label: 'L2 Connections'
    },
    {
      key: '4',
      icon: <L3Icon />,
      label: 'L3 connections'
    },
    {
      key: '5',
      icon: <CRIcon />,
      label: 'CloudRouter'
    },
    {
      key: '6',
      icon: <SettingIcon />,
      label: 'Settings'
    }
  ]

  return (
    <div className={styles.appWrapper}>
      <Authenticate>
        <MainMenuMobileDrawer
          onClose={falseDrawer}
          open={mainMobileDrawer}
          items={items}
          activeKeys={activeKeys}
          setActiveKeys={setActiveKeys}
        />
        <Headroom disableInlineStyles>
          <Suspense fallback=''>
            <NavMain />
          </Suspense>
        </Headroom>
        <Flex vertical={isMobile} className={styles.container}>
          {isMobile ? (
            <Flex
              role='none'
              style={{ background: mainColor, cursor: 'pointer' }}
              className={styles.network}
              gap={12}
              align='center'
              onClick={toggleDrawer}
            >
              <img src={NetIcon} alt='network' />
              {!collapsed && <Text.NormalLarge color='#fff'>NETWORK</Text.NormalLarge>}
            </Flex>
          ) : (
            <SliderCustom collapsible collapsed={collapsed} onCollapse={setCollapsed} className={styles.slider}>
              <Flex vertical style={{ background: mainColor }} className={styles.network}>
                <Flex justify='flex-end' className={styles.collapseBtn}>
                  {!collapsed ? (
                    <DoubleLeftOutlined onClick={trueCollapse} role='none' />
                  ) : (
                    <DoubleRightOutlined onClick={falseCollapse} role='none' />
                  )}
                </Flex>
                <Flex gap={12} align='center' style={{ marginTop: 16 }}>
                  <img src={NetIcon} alt='network' />
                  {!collapsed && <Text.NormalLarge color='#fff'>NETWORK</Text.NormalLarge>}
                </Flex>
              </Flex>
              <Menu
                openKeys={openKeys}
                onOpenChange={(k) => setOpenKeys(k)}
                onSelect={(e) => {
                  setActiveKeys(e.selectedKeys)
                }}
                className={styles.menu}
                selectedKeys={activeKeys}
                mode='inline'
                items={items}
                expandIcon={(iconInfo) => (iconInfo.isOpen ? <DownOutlined /> : <RightOutlined />)}
              />
            </SliderCustom>
          )}
          <Outlet />
        </Flex>
      </Authenticate>
    </div>
  )
}

export default Layout
