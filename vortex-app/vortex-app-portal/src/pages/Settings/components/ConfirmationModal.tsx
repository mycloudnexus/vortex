import type { ReactElement, ReactNode } from 'react'
import { StyledModal } from './styled'
import { Flex } from 'antd'
import { ReactComponent as CCWarning } from '@/assets/icon/warning-circle.svg'
import Text from '@/components/Text'
interface ConfirmationModalProps {
  open: boolean
  handleOk: () => void
  handleCancel: () => void
  text: ReactNode
}
const ConfirmationModal = ({ text, handleCancel, handleOk, open }: ConfirmationModalProps): ReactElement => {
  return (
    <StyledModal
      centered
      title={undefined}
      open={open}
      okText='Yes, continue'
      onOk={handleOk}
      onCancel={handleCancel}
      closable={false}
      cancelText='Cancel'
      $containerWidth='25rem'
      okButtonProps={{ style: { backgroundColor: '#FF4D4F' } }}
      data-testid='warning-modal'
    >
      <Flex gap={10} vertical style={{ marginLeft: '30px' }}>
        <Flex align='start' gap={5}>
          <CCWarning />
          <Text.NormalMedium>{text}</Text.NormalMedium>
        </Flex>
      </Flex>
    </StyledModal>
  )
}

export default ConfirmationModal
