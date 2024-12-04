import type { ReactElement } from 'react'

import { ReactComponent as CCInfo } from '@/assets/icon/info.svg'

import { Tooltip as AntTooltip, TooltipProps, Typography } from 'antd'

type CustomToolTipProps = { message?: string; orgId?: string }
type TooltipPropsCustom = CustomToolTipProps & TooltipProps

const Tooltip = ({ message = '', orgId = '', ...rest }: TooltipPropsCustom): ReactElement => {
  const UrlGenerate = (val: string): string => `${window.location.origin}/${val}/login`

  return (
    <AntTooltip
      title={
        orgId ? (
          <Typography.Paragraph
            copyable={{ text: UrlGenerate(orgId) }}
            style={{ display: 'flex', alignItems: 'flex-start' }}
            data-testid='tooltip'
          >
            Login URL:
            <br />
            {UrlGenerate(orgId)}
          </Typography.Paragraph>
        ) : (
          message
        )
      }
      arrow
      {...rest}
    >
      <span data-testid='mocked-svg' style={{ display: 'flex', justifyContent: 'center' }}>
        <CCInfo />
      </span>
    </AntTooltip>
  )
}

export default Tooltip
