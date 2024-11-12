import type { ReactElement } from 'react'
import type { Company } from '@/stores/company.store'
import { Flex, Form, FormInstance, ModalProps } from 'antd'

import { CustomInput, StyledModal } from './styled'
import Tooltip from './Tooltip'

export interface CustomerCompanyModalProps<T> extends ModalProps {
  title: string
  isModalOpen: boolean
  handleOk: () => void
  handleCancel: () => void
  form: FormInstance
  initialValues: T
  companies: Company[]
  name: string
  type?: 'add' | 'update'
}

const CustomerCompanyModal = <T extends object>({
  title,
  isModalOpen,
  handleOk,
  handleCancel,
  form,
  initialValues,
  companies,
  name,
  type = 'add',
  ...rest
}: CustomerCompanyModalProps<T>): ReactElement => {
  return (
    <StyledModal
      centered
      $withDivider
      title={title}
      open={isModalOpen}
      onOk={handleOk}
      onCancel={handleCancel}
      {...rest}
    >
      <Form form={form} name={name} initialValues={initialValues} layout='vertical' style={{ padding: '20px' }}>
        <Form.Item
          name='title'
          label={
            <Flex align='center' gap={5}>
              Customer company name
              <span style={{ color: 'red' }}>*</span>
            </Flex>
          }
          required={false}
          rules={[
            { required: true, message: 'Customer name cannot be empty' },
            {
              validator: async (_, value) => {
                const exist = companies.find((company) => company.title === value)
                if (exist) {
                  return Promise.reject(new Error('Customer name cannot be duplicated'))
                }
                return Promise.resolve()
              }
            }
          ]}
        >
          <CustomInput placeholder='Please enter' />
        </Form.Item>

        <Form.Item
          name='shortName'
          label={
            <Flex gap={5} align='center'>
              Customer company URL short name
              <Tooltip
                title='This is used for login url generation. Please be careful as it cannot be changed once provided.'
                color='black'
                placement='topRight'
              />
              <span style={{ color: 'red' }}>*</span>
            </Flex>
          }
          required={false}
          rules={[
            { required: true, message: ' Customer company URL short name cannot be empty' },
            {
              pattern: /^[a-z0-9-]{1,20}$/,
              message: 'Only lowercase letters, numbers, and hyphens are allowed. No more than 20 characters.'
            }
          ]}
          help='Only lower case letters, numbers, - , allowed. No more than 20 characters in total'
        >
          <CustomInput placeholder='Please enter' disabled={type === 'update'} />
        </Form.Item>
      </Form>
    </StyledModal>
  )
}

export default CustomerCompanyModal
