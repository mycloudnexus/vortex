import { Suspense, useEffect, useState } from 'react'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import Headroom from 'react-headroom'
import { useBoolean } from 'usehooks-ts'
import { styled } from 'styled-components'
import { Flex, Menu, Layout as AntdLayout } from 'antd'
import { DoubleLeftOutlined, DoubleRightOutlined, DownOutlined, RightOutlined } from '@ant-design/icons'
import NetIcon from '@/assets/icon/network.svg'
import { ReactComponent as DashboardIcon } from '@/assets/icon/dashboard.svg'
import { ReactComponent as DCIcon } from '@/assets/icon/dcport.svg'
import { ReactComponent as CRIcon } from '@/assets/icon/cloudrouter.svg'
import { ReactComponent as L2Icon } from '@/assets/icon//l2.svg'
import { ReactComponent as L3Icon } from '@/assets/icon/l3.svg'
import { ReactComponent as SettingIcon } from '@/assets/icon/setting.svg'
import useDeviceDetect from '@/hooks/useDeviceDetect'
import useElementSize from '@/hooks/useElementSize'
import { useAppStore } from '@/stores/app.store'
import BreadCrumb from '../BreadCrumb'
import Text from '../Text'
import Authenticate from '../Access/Authenticate'
import MainMenuMobileDrawer from './MainMenuMobileDrawer'
import NavMain from './NavMain'
import * as styles from './index.module.scss'

const { Sider } = AntdLayout

export const SliderCustom = styled(Sider)<{ $mainColor: string }>`
  .ant-menu-submenu-selected {
    svg {
      path {
        stroke: ${(props) => props.$mainColor};
      }
    }
  }
  .ant-menu-item-selected {
    svg {
      path {
        fill: ${(props) => props.$mainColor};
        stroke: ${(props) => props.$mainColor};
      }
      circle {
        stroke: ${(props) => props.$mainColor};
      }
    }
  }
`
const Layout = () => {
  const { mainColor } = useAppStore()

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
        if (m.children) {
          setOpenKeys([m.key])
          for (const c of m.children) {
            if ((c as any).regex?.test(location.pathname)) {
              setActiveKeys([m.key, c.key])
            }
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
      regex: /^\/ports(\/.*)?$/,
      children: [
        {
          key: '2-1',
          label: 'View all',
          regex: /^\/ports(\/.*)?$/,
          onClick: () => {
            navigate('/ports')
          }
        },
        {
          key: '2-2',
          label: 'Add new',
          regex: /^\/ports\/create(\/.*)?$/,
          onClick: () => {
            navigate('/ports/create')
          }
        }
      ]
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
      label: 'Settings',
      children: [
        {
          key: '6-1',
          label: 'Users',
          onClick: () => {
            navigate('/settings/users')
          }
        },
        {
          key: '6-2',
          label: 'Customer company',
          onClick: () => {
            navigate('/settings/customer-company')
          }
        }
      ]
    }
  ]

  const [navSize, navRef] = useElementSize()

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
            <NavMain ref={navRef} />
          </Suspense>
        </Headroom>
        <Flex
          vertical={isMobile}
          className={styles.container}
          style={{
            height: `calc(100vh - ${navSize.height + 20}px)`
          }}
        >
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
            <SliderCustom
              collapsible
              collapsed={collapsed}
              onCollapse={setCollapsed}
              className={styles.slider}
              $mainColor={mainColor}
            >
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
          <Flex vertical justify='start' align='start' style={{ width: '100%' }}>
            <BreadCrumb />
            <Outlet />
          </Flex>
        </Flex>
      </Authenticate>
    </div>
  )
}

export default Layout
