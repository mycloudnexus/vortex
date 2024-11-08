import { forwardRef } from 'react'
import { Link } from 'react-router-dom'
import SettingsMenu from './SettingsMenu'
import { Flex } from 'antd'
import * as styles from './index.module.scss'

const NavMain = forwardRef<any, any>((_, ref) => {
  const userIsLoggedIn = true
  return (
    <Flex justify='space-between' align='center' ref={ref}>
      <nav>
        <Flex align='center' gap={16} className={styles.pageNav}>
          <Link to={''}>
            <img alt='Vortex' src={''} />
          </Link>
          <Link to={''}>
            <h3>Vortex</h3>
          </Link>
        </Flex>
      </nav>

      <nav data-iht='user-nav' className='user-nav'>
        {userIsLoggedIn && <SettingsMenu />}
      </nav>
    </Flex>
  )
})

NavMain.displayName = 'NavMain'

export default NavMain
