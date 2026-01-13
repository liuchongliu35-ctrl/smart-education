import React, { useEffect, useState } from 'react';
import { Avatar, Button, ConfigProvider, Flex, Form, DatePicker } from 'antd';
import style from '@/pages/homeWork/homeWork.module.css'
import ModalLoadingComponent from '../Loading/modalLoading'
import { CheckCard } from '@ant-design/pro-components'
import { UserOutlined } from '@ant-design/icons';
import { renderAvatar } from '../class/renderAvatar';

const AvatartItem = [
  <Avatar size={42} icon={<UserOutlined />} style={{ marginRight: 16, background: '#f56a00' }} />,
  <Avatar size={42} icon={<UserOutlined />} style={{ marginRight: 16, background: '#283be5' }} />,
  <Avatar size={42} icon={<UserOutlined />} style={{ marginRight: 16, background: '#310f9e' }} />,
]


const PublishForm = ({ onSubmit, classList, hid }) => {
  const [form] = Form.useForm()
  const config = {
    rules: [{ type: 'object', required: true, message: '请选择截止时间' }],
  }
  console.log(classList)

  const handleSubmit = (values) => {
    const prop = {
      ...values,
      cid: parseInt(values.cid, 10) || 0,
      deadline: values.deadline?.format('YYYY-MM-DD HH:mm:ss'),
      ptId: hid
    }
    console.log(prop)
    if (onSubmit) {
      onSubmit(prop)
    }
  }



  return (


    <ConfigProvider>
      <>
        <Form
          form={form}
          onFinish={handleSubmit}
          layout="vertical"
          style={{ width: 300 }}>
          <Form.Item name="cid" style={{ marginLeft: '18%' }} >
            <CheckCard.Group style={{ width: '80%' }}>
              {classList?.map((item, index) => (
                <CheckCard
                  size={'small'}
                  title={item.cname}
                  avatar={renderAvatar(item.cid)}
                  value={item.cid}
                  style={{ width: '100%' }}
                />
              ))}
            </CheckCard.Group>
          </Form.Item>
          <Form.Item
            style={{ marginLeft: '15%' }}
            name="deadline"
            label="截止时间"
            {...config}>
            <DatePicker showTime format="YYYY-MM-DD HH:mm:ss" placeholder='请选择截止时间' />
          </Form.Item>
          <Form.Item>
            <div style={{ width: 'max-content', margin: 'auto' }}>
              <button
                className={style.functionBtn}
                type="primary"
                htmlType="submit"
                style={{ height: 30 }}
              >
                确认发布
              </button>
            </div>
          </Form.Item>
        </Form>

      </>


    </ConfigProvider>

  )
}
export default PublishForm