import { styled } from 'styled-components'
import { Button, Card, Form, Input, Modal, Table, TableProps, Tabs } from 'antd'
import { ICompany } from '@/services/types'

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
})<{ $width?: string; $backgroundColor: string }>`
  &.ant-btn {
    background-color: ${({ $backgroundColor }) => $backgroundColor};
    color: white;
    border: ${({ $backgroundColor }) => `solid ${$backgroundColor}`};
    border-radius: 5px;
    font-weight: bold;
    padding: 20px 30px;
    font-size: 16px;
    width: ${({ $width }) => $width};
  }

  &.ant-btn[disabled] {
    background-color: ${({ $backgroundColor }) => `solid ${$backgroundColor}`};
    color: white;
    border-color: ${({ $backgroundColor }) => `solid ${$backgroundColor}`};
    cursor: not-allowed;
    opacity: 0.7;
    width: ${({ $width }) => $width};
  }

  &.ant-btn[disabled]:hover {
    background-color: ${({ $backgroundColor }) => `solid ${$backgroundColor}`};
    color: white;
    border-color: ${({ $backgroundColor }) => `solid ${$backgroundColor}`};
    width: ${({ $width }) => $width};
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

export const StyledTable = styled(Table)<TableProps<ICompany>>`
  .ant-table-thead > tr > th {
    border-top: 1px #f0f0f0 solid !important;
    border-bottom: 1px #f0f0f0 solid !important;
    background-color: unset !important;
  }
  .ant-table-thead > tr > th::before {
    width: unset !important;
  }
`

export const StyledTabs = styled(Tabs).withConfig({
  shouldForwardProp: (props) => !props.startsWith('$')
})<{ $mainColor: string }>`
  width: 100%;
  .ant-tabs-tab-active .ant-card {
    border-color: ${({ $mainColor }) => $mainColor};
  }

  .ant-card {
    width: 300px;
    height: 100px;
    display: flex;
    align-items: center;
  }
`

export const StyledInfoTab = styled(Card)`
  border: unset;
  .ant-card-body {
    padding: 0;
  }
`

export const StyledFormItem = styled(Form.Item)`
  margin-bottom: unset !important;
  .ant-form-item-row {
    flex-direction: row !important;
    width: 75% !important;
    justify-content: space-between !important;
    align-items: center !important;
  }

  .ant-form-item-label {
    order: 1;
    padding: 0 !important;
  }

  .ant-form-item-control {
    order: 2;
    flex: 0 0;
  }
`

export const StyledForm = styled(Form)`
  width: 100%;
  overflow: auto;
  height: 70vh;
  display: flex;
  flex-direction: column;
  gap: 15px;
`
