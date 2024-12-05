import { Drawer, Menu } from 'antd'
import * as styles from './index.module.scss'
import { useAppStore } from '@/stores/app.store'
import { styled } from 'styled-components'

const MenuCustom = styled(Menu)<{ $mainColor: string }>`
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

type Props = {
  open: boolean
  onClose: () => void
  items: any
  activeKeys: string[]
  setActiveKeys: (activeKeys: string[]) => void
}

const MainMenuMobileDrawer = ({ open, onClose, items, activeKeys, setActiveKeys }: Props) => {
  const { mainColor } = useAppStore()
  return (
    <Drawer
      styles={{
        header: {
          display: 'none'
        }
      }}
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
        $mainColor={mainColor}
      />
    </Drawer>
  )
}

export default MainMenuMobileDrawer
