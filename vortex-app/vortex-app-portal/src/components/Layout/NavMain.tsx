import { Link, useLocation } from 'react-router-dom'
import SettingsMenu from './SettingsMenu'
import { Flex } from 'antd'
import * as styles from './index.module.scss'
import { useMemo } from 'react'
import clsx from 'clsx'
import { useAppStore } from '@/stores/app.store'

const NavMain = () => {
  const { mainColor } = useAppStore()
  const userIsLoggedIn = true
  const location = useLocation()
  const menu = useMemo(
    () => [
      {
        label: 'Network',
        match: [/^\/$/, /^\/network/],
        key: 'network',
        href: '/'
      },
      {
        label: 'Pricing',
        match: [/^\/pricing/],
        key: 'pricing',
        href: '/pricing'
      }
    ],
    []
  )
  return (
    <Flex justify='space-between' align='center' style={{ width: '100%' }}>
      <nav>
        <Flex align='center' gap={16} className={styles.pageNav}>
          <Link to={''}>
            <img alt='Vortex' src={''} />
          </Link>
          <Link to={''}>
            <h3>Vortex</h3>
          </Link>
          <Flex align='center'>
            {menu.map((item) => {
              const active = () => {
                for (const i of item.match) {
                  if (i.test(location.pathname)) {
                    return true
                  }
                }
                return false
              }
              return (
                <Link
                  key={item.key}
                  to={item.href}
                  className={clsx([styles.menuItem])}
                  style={{ color: active() ? mainColor : '#fff' }}
                >
                  {item.label}
                  {active() && <div className={styles.menuItemActive} style={{ background: mainColor }} />}
                </Link>
              )
            })}
          </Flex>
        </Flex>
      </nav>

      <nav data-iht='user-nav' className='user-nav'>
        {userIsLoggedIn && <SettingsMenu />}
      </nav>
    </Flex>
  )
}

export default NavMain
