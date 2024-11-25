import type { ReactElement } from 'react'

import { ReactComponent as CCInfo } from '@/assets/icon/info.svg'

import { Tooltip as AntTooltip, TooltipProps, Typography } from 'antd'

type CustomToolTipProps = { shortName?: string; message?: string }
type TooltipPropsCustom = CustomToolTipProps & TooltipProps

const Tooltip = ({ shortName = '', message = '', ...rest }: TooltipPropsCustom): ReactElement => {
  const UrlGenerate = (val: string): string => `https://consoleconnect/${val}.com`

  return (
    <AntTooltip
      title={
        shortName ? (
          <Typography.Paragraph copyable={{ text: shortName }} style={{ display: 'flex', alignItems: 'flex-start' }}>
            Login URL:
            <br />
            {UrlGenerate(shortName)}
          </Typography.Paragraph>
        ) : (
          message
        )
      }
      arrow
      {...rest}
    >
      <span data-testid='mocked-svg'>
        <CCInfo />
      </span>
    </AntTooltip>
  )
}

export default Tooltip
