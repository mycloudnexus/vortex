import Text from '@/components/Text'
import { Card, Flex, Tag } from 'antd'
import { ReactElement } from 'react'

interface CardTabProps {
  isEnabled: boolean
  title: string
  titleExtension?: string
  description: string
  icon?: ReactElement
}
const CardTab = ({ isEnabled, title, description, titleExtension, icon }: CardTabProps): ReactElement => {
  return (
    <Card style={{ borderRadius: '5px' }}>
      <Flex justify='center' align='center' gap={10}>
        <div data-testid='tab-icon'>{icon}</div>
        <Flex vertical gap={5} align='start' justify='center'>
          <Flex justify='space-between' align='center' style={{ width: '100%' }}>
            <Text.BoldLarge style={{ margin: 0 }}>
              {title}
              {titleExtension && (
                <Text.BoldLarge style={{ color: '#000', opacity: '45%' }}>{titleExtension}</Text.BoldLarge>
              )}
            </Text.BoldLarge>
            {isEnabled && (
              <Tag color='green' style={{ height: '80%', border: 'unset' }}>
                Enabled
              </Tag>
            )}
          </Flex>
          <Text.NormalMedium style={{ color: '#000', opacity: '45%', whiteSpace: 'wrap', textAlign: 'start' }}>
            {description}
          </Text.NormalMedium>
        </Flex>
      </Flex>
    </Card>
  )
}

export default CardTab
