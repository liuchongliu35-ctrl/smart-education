import React, { useEffect, useState } from 'react'
import style from './entryPage.module.css'
import BackgroundImg from '../../assets/svg/小智备课Background.svg'
import { RightIcon } from '../../assets/icons'
import { ArrowRightOutlined } from '@ant-design/icons';
import { useLocation, useNavigate } from 'react-router-dom';
import { Input, Checkbox, Form, Row, Col, Radio, message } from 'antd'
import LoginComponent from './login'
import { postRegisterUser } from '../../apis/user';


const Introduce = ({ handleClick }) => (
  <div style={{ width: '100%', marginTop: 150 }}>
    <div style={{ fontSize: 40, fontFamily: 'youshe', color: '#435fff', lineHeight: 1.5 }}>小智备课</div>
    <div style={{ fontSize: 26, fontFamily: 'siyuan', color: '#000000', lineHeight: 1.5 }}>AI智能教学助手，让备课更高效</div>
    <div style={{ fontSize: 16, color: '#6e6e6e', lineHeight: 1.2, marginTop: 10 }}>AI making lesson preparation more efficient</div>
    <div style={{ marginTop: 40, fontSize: 20, fontFamily: 'siyuan', color: '#303030' }}>
      <span style={{ marginRight: 10 }}><RightIcon /></span>AI智能备课，释放教师创造力
    </div>
    <div style={{ marginTop: 30, fontSize: 20, fontFamily: 'siyuan', color: '#303030' }}>
      <span style={{ marginRight: 10 }}><RightIcon /></span>精准题目生成，分层教学无忧
    </div>
    <div style={{ marginTop: 30, fontSize: 20, fontFamily: 'siyuan', color: '#303030' }}>
      <span style={{ marginRight: 10 }}><RightIcon /></span>多维学情分析，教学决策有据可依
    </div>
    <button className={style.loginButton} onClick={() => handleClick()} ><ArrowRightOutlined style={{ marginRight: 10 }} />立即登录</button>
  </div>
)



const Register = ({ onChange, handleClick }) => {
  const handleSubmit = async (values) => {
    const prop = {
      ...values,
      isActive: 1
    }
    await postRegisterUser(prop)
    message.success('账号注册成功！')
    setTimeout(() => {
      handleClick()
    }, 1000);
    console.log(prop)
  }

  return (
    <div style={{ width: '100%', height: 'max-content', paddingBlock: 30, background: '#fff', display: 'flex', alignItems: 'center', flexDirection: 'column', justifyContent: 'center', marginTop: 100, boxShadow: '0px 0px 8px #6e6e6e45', borderRadius: 10 }}>
      <div style={{ width: '60%' }}>
        <div style={{ fontSize: 30, marginBottom: 20, fontFamily: 'siyuan', lineHeight: 1.5, textAlign: 'center' }}>教师注册</div>
        <Form
          onFinish={handleSubmit}
          layout="vertical"
          labelCol={{ flex: '30px' }}
          labelAlign="left"
          wrapperCol={{ flex: 0.5 }}
          colon={false}>
          <Form.Item label="用户名" name="account" rules={[{ required: true, message: '请输入用户名！' }]}>
            <Input />
          </Form.Item>

          <Form.Item label="密码" name="password" rules={[{ required: true, message: '请输入密码！' }]}>
            <Input.Password />
          </Form.Item>
          <Form.Item label="确认密码" name="confirm" dependencies={['password']} rules={[
            {
              required: true,
              message: '请确认您的密码!',
            },
            ({ getFieldValue }) => ({
              validator(_, value) {
                if (!value || getFieldValue('password') === value) {
                  return Promise.resolve();
                }
                return Promise.reject(new Error('当前密码与之前不同!'));
              },
            }),
          ]}>
            <Input.Password />
          </Form.Item>
          <Row gutter={8}>
            <Col span={12}>
              <Form.Item label="电话号码" name="phone" rules={[{ required: true, message: '请输入电话号码！' }]}>
                <Input
                  type="number"
                  style={{
                    height: 28,
                    padding: '2px 8px',
                    fontSize: 13
                  }}
                />
              </Form.Item>
            </Col>
            <Col span={2}></Col>
            <Col span={10}>
              <Form.Item label="性别" name="sex" rules={[{ required: true, message: '请选择您的性别！' }]}>
                <Radio.Group onChange={onChange} >
                  <Radio value={'男'}>男</Radio>
                  <Radio value={'女'}>女</Radio>
                </Radio.Group>
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={8}>
            <Col span={12}>
              <Form.Item label="邮箱" name="email" rules={[{ required: true, message: '请输入您的邮箱！' }]}>
                <Input />
              </Form.Item>
            </Col>
            <Col span={2}></Col>
            <Col span={10}>

              <Form.Item label="姓名" name="name" rules={[{ required: true, message: '请输入您的姓名！' }]}>
                <Input />
              </Form.Item>
            </Col>
          </Row>



          <Form.Item>
            <div style={{ width: 'max-content', margin: 'auto', marginTop: 40 }}><button htmlType="submit" className={style.loginBtn} style={{ paddingInline: 80 }}>注册</button></div>
          </Form.Item>

        </Form>
      </div>
    </div>
  )
}

const EntryPage = () => {
  const location = useLocation()
  const navigate = useNavigate()
  const token = localStorage.getItem('authToken')
  const [messageApi, contextHolder] = message.useMessage()
  useEffect(() => {
    if (token) {
      navigate('/home')
    }
    if (location.state?.showQuitSuccess) {
      messageApi.open({
        type: 'success',
        content: '账号退出成功！',
      })

      // 清除状态，避免刷新后重复显示
      window.history.replaceState({}, document.title);
    }
  }, [])


  const handleNavigation = () => {
    setCureentState(2)
  }



  const onChange = (e) => {
    console.log('radio checked', e.target.value)
    setValue(e.target.value)
  }
  const [currentState, setCureentState] = useState(1)

  function render(e) {
    switch (e) {
      case 1:
        return <Introduce handleClick={handleNavigation} />
      case 2:
        return <LoginComponent />
      case 3:
        return <Register onChange={onChange} handleClick={handleNavigation} />

      default:
        break;
    }
  }



  return (
    <>
      <div style={{ width: '100%', background: '#F2F5FF', minWidth: 1400 }}>
        {contextHolder}
        <div style={{ width: '90%', margin: 'auto', display: 'flex', position: 'relative' }}>
          <div className={style.header}>
            <span style={{ fontSize: 28, fontFamily: 'youshe', color: '#435fff' }}>小智备课</span>
            <span style={{ cursor: 'pointer', fontSize: 16, fontFamily: 'siyuan', marginLeft: 30 }}>首页</span>
            <span style={{ cursor: 'pointer', fontSize: 16, fontFamily: 'siyuan', marginLeft: 30, color: '#727272' }}>合作</span>
            <span style={{ cursor: 'pointer', fontSize: 16, fontFamily: 'siyuan', marginLeft: 30, color: '#727272' }}>了解更多</span>
            <span style={{ flex: 1 }}></span>
            <button className={style.registerBtn} onClick={() => setCureentState(3)}>教师注册</button>
            <button className={style.loginBtn} onClick={() => setCureentState(2)}>登录</button>
          </div>
          <div style={{ flex: 1, display: 'flex', paddingInline: 30 }}>
            {render(currentState)}

          </div>
          <div style={{ width: '62%' }}>
            <img src={BackgroundImg} />
          </div>
        </div>
      </div>
    </>
  )
}

export default EntryPage  