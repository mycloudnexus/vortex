import { styled } from 'styled-components'
import { Button, Input, Modal, Table, TableProps } from 'antd'
import { Company } from '@/stores/company.store'

export const StyledModal = styled(Modal).withConfig({
  shouldForwardProp: (props) => !props.startsWith('$')
})<{ $withDivider?: boolean; $containerWidth?: string }>`
  .ant-modal-content {
    border-radius: 10px;
    width: ${({ $containerWidth = '40rem' }) => $containerWidth};
  }
  .ant-modal-header {
    border-bottom: ${({ $withDivider }) => ($withDivider ? '2px solid #f0f0f0' : 'none')};
    padding-bottom: 20px;
  }
  .ant-btn {
    border-radius: 5px;
  }
`
export const CustomInput = styled(Input)`
  border-radius: 4px;
`

export const StyledButton = styled(Button).withConfig({
  shouldForwardProp: (props) => !props.startsWith('$')
})<{ $backgroundColor: string }>`
  &.ant-btn {
    background-color: ${({ $backgroundColor }) => $backgroundColor};
    color: white;
    border: ${({ $backgroundColor }) => `solid ${$backgroundColor}`};
    border-radius: 5px;
    font-weight: bold;
    padding: 20px 30px;
    font-size: 16px;
  }

  &.ant-btn:hover {
    color: #000;
    background-color: #fff;
  }
`

export const StyledWrapper = styled('div').withConfig({
  shouldForwardProp: (props) => !props.startsWith('$')
})<{ $backgroundColor: string }>`
  background-color: ${({ $backgroundColor }) => $backgroundColor};
  border-radius: 5px;
  box-shadow: 3px 0 #000;
  padding: 0.25em 0.45em;
  display: flex;
  justify-content: center;
`

export const StyledTable = styled(Table)<TableProps<Company>>`
  .ant-table-thead > tr > th {
    border-top: 1px #f0f0f0 solid !important;
    border-bottom: 1px #f0f0f0 solid !important;
    background-color: unset !important;
  }
  .ant-table-thead > tr > th::before {
    width: unset !important;
  }
`
