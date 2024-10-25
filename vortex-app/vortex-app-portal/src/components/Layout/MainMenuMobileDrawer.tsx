import { Drawer, Menu } from 'antd'
import * as styles from './index.module.scss'
import { useAppStore } from '@/stores/app.store'
import { styled } from 'styled-components'

type Props = {
  open: boolean
  onClose: () => void
  items: any
  activeKeys: string[]
  setActiveKeys: (activeKeys: string[]) => void
}

const MainMenuMobileDrawer = ({ open, onClose, items, activeKeys, setActiveKeys }: Props) => {
  const { mainColor } = useAppStore()
  const MenuCustom = styled(Menu)`
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
  return (
    <Drawer
      headerStyle={{ display: 'none' }}
      rootClassName={styles.mobileDrawer}
      open={open}
      width='100vw'
      onClose={onClose}
      placement='left'
      mask={false}
    >
      <MenuCustom
        onSelect={(e) => {
          setActiveKeys(e.selectedKeys)
          onClose()
        }}
        className={styles.menu}
        selectedKeys={activeKeys}
        mode='inline'
        items={items}
      />
    </Drawer>
  )
}

export default MainMenuMobileDrawer
