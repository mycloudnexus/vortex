import Sider from 'antd/es/layout/Sider'
import { styled } from 'styled-components'
const SliderCustom = (mainColor: string) => styled(Sider)`
  .ant-menu-submenu-selected {
    .icon-dashboard {
      path {
        fill: ${mainColor};
      }
    }
    .icon-dc {
      path {
        stroke: ${mainColor};
      }
    }
    .icon-l2 {
      path {
        fill: ${mainColor};
      }
    }
    .icon-l3 {
      path {
        stroke: ${mainColor};
      }
      circle {
        stroke: ${mainColor};
      }
    }
    .icon-cr {
      path:not(.special-path) {
        stroke: ${mainColor};
      }
      .special-path {
        fill: ${mainColor};
      }
    }
    .icon-setting {
      path {
        fill: ${mainColor};
      }
    }
  }
  .ant-menu-item-selected {
    .icon-dashboard {
      path {
        fill: ${mainColor};
      }
    }
    .icon-dc {
      path {
        stroke: ${mainColor};
      }
    }
    .icon-l2 {
      path {
        fill: ${mainColor};
      }
    }
    .icon-l3 {
      path {
        stroke: ${mainColor};
      }
      circle {
        stroke: ${mainColor};
      }
    }
    .icon-cr {
      path:not(.special-path) {
        stroke: ${mainColor};
      }
      .special-path {
        fill: ${mainColor};
      }
    }
    .icon-setting {
      path {
        fill: ${mainColor};
      }
    }
  }
`

export { SliderCustom }
