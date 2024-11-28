import { Link } from 'react-router-dom'
import SettingsMenu from './SettingsMenu'
import { Flex } from 'antd'
import { DefaultLogo } from './Icon'
import { ENV } from '@/constant'
import * as styles from './index.module.scss'

const NavMain = () => {
  const userIsLoggedIn = true
  return (
    <Flex justify='space-between' align='center'>
      <nav>
        <Flex align='center' gap={16} className={styles.pageNav}>
          <Link to={''}>{ENV.COMPANY_LOGO_URL ? <img src={ENV.COMPANY_LOGO_URL} alt='logo' /> : <DefaultLogo />}</Link>
          <Link to={''}>
            <h3>goPartnerConnect</h3>
          </Link>
        </Flex>
      </nav>

      <nav data-iht='user-nav' className='user-nav'>
        {userIsLoggedIn && <SettingsMenu />}
      </nav>
    </Flex>
  )
}

export default NavMain
