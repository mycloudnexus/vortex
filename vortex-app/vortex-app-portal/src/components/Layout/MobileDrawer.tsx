import { Avatar, Col, Drawer, Flex, Row } from 'antd'
import * as styles from './index.module.scss'
import { Fragment } from 'react'
import { CloseOutlined, LogoutOutlined } from '@ant-design/icons'

type Props = {
  open: boolean
  onClose: () => void
}

const MobileDrawer = ({ open, onClose }: Props) => {
  const menus = [
    { title: 'Tim Tim', onClick: () => {}, icon: <Avatar size='small'>TP</Avatar> },
    { title: 'Dashboard', onClick: () => {} },
    { title: 'Logout', onClick: () => {}, icon: <LogoutOutlined style={{ color: '#fff', cursor: 'pointer' }} /> }
  ]
  return (
    <Drawer
      headerStyle={{ display: 'none' }}
      className={styles.drawer}
      open={open}
      width={`calc(100vw - 145px)`}
      onClose={onClose}
    >
      <Row style={{ width: '100%' }}>
        {menus.map((item, index) => (
          <Fragment key={item.title}>
            <Col span={3}>
              <div className={styles.menuIcon}>{item.icon}</div>
            </Col>
            <Col span={21}>
              <Flex align='center' justify='space-between' className={styles.menuItem}>
                <div className={index !== 0 ? styles.avatarItem : styles.userName}>{item.title}</div>
                {index === 0 && <CloseOutlined style={{ color: '#fff', cursor: 'pointer' }} onClick={onClose} />}
              </Flex>
            </Col>
          </Fragment>
        ))}
      </Row>
    </Drawer>
  )
}

export default MobileDrawer
