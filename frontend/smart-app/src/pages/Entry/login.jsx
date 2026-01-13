import { AlipayCircleOutlined, LockOutlined, MobileOutlined, TaobaoCircleOutlined, UserOutlined } from '@ant-design/icons';
import { LoginForm, ProConfigProvider, ProFormCaptcha, ProFormCheckbox, ProFormText, setAlpha } from '@ant-design/pro-components';
import { Space, Tabs, message, theme, Form, Input } from 'antd';
import { useState } from 'react';
import { postUserLogin } from '../../apis/user';
import { useNavigate } from 'react-router-dom';


const LoginComponent = () => {
    const [loginType, setLoginType] = useState('account')
    const navigate = useNavigate()

    const handleOnFinish = async (values) => {
        if (values) {
            const res = await postUserLogin(values)
            console.log(res.data)
            localStorage.setItem('authToken', res.data.token)
            sessionStorage.setItem('currentList', '预习任务')
            sessionStorage.setItem('currentShow', '1')
            navigate('/home', { state: { showLoginSuccess: true } })
        } else {
            message.warning('账号或密码不正确！')
        }
    }

    return (
        <ProConfigProvider hashed={false}>
            <div style={{ width: '100%', height: '60vh', minHeight: 500, background: '#ffffff9b', display: 'flex', alignItems: 'center', flexDirection: 'column', marginTop: 160, boxShadow: '0px 0px 8px #6e6e6e45', borderRadius: 10 }}>
                <LoginForm
                    onFinish={handleOnFinish}
                    title={<div style={{ fontSize: 24, fontFamily: 'youshe', color: '#435fff', textAlign: 'center', lineHeight: 1 }}>小智备课</div>}
                    subTitle="高效的智能备课助手"
                >
                    <Tabs
                        centered
                        activeKey={loginType}
                        onChange={(activeKey) => setLoginType(activeKey)}
                    >
                        <Tabs.TabPane key={'account'} tab={'账号密码登录'} />
                        <Tabs.TabPane key={'phone'} tab={'手机号登录'} />
                    </Tabs>
                    {loginType === 'account' && (
                        <>
                            <ProFormText
                                name="username"
                                fieldProps={{
                                    size: 'large',
                                    prefix: <UserOutlined className={'prefixIcon'} />,
                                }}
                                placeholder={'请输入您的用户名'}
                                rules={[
                                    {
                                        required: true,
                                        message: '请输入用户名!',
                                    },
                                ]}
                            />
                            <ProFormText.Password
                                placeholder={'请输入您的密码'}
                                name="password"
                                fieldProps={{
                                    size: 'large',
                                    prefix: <LockOutlined className={'prefixIcon'} />,
                                }}
                            />
                        </>
                    )}
                    {loginType === 'phone' && (
                        <>
                            <ProFormText
                                fieldProps={{
                                    size: 'large',
                                    prefix: <MobileOutlined className={'prefixIcon'} />,
                                }}
                                name="mobile"
                                placeholder={'手机号'}
                                rules={[
                                    {
                                        required: true,
                                        message: '请输入手机号！',
                                    },
                                    {
                                        pattern: /^1\d{10}$/,
                                        message: '手机号格式错误！',
                                    },
                                ]}
                            />
                            <ProFormCaptcha
                                fieldProps={{
                                    size: 'large',
                                    prefix: <LockOutlined className={'prefixIcon'} />,
                                }}
                                captchaProps={{
                                    size: 'large',
                                }}
                                placeholder={'请输入验证码'}
                                captchaTextRender={(timing, count) => {
                                    if (timing) {
                                        return `${count} ${'获取验证码'}`;
                                    }
                                    return '获取验证码';
                                }}
                                name="captcha"
                                rules={[
                                    {
                                        required: true,
                                        message: '请输入验证码！',
                                    },
                                ]}
                                onGetCaptcha={async () => {
                                    message.success('获取验证码成功！验证码为：1234');
                                }}
                            />
                        </>
                    )}
                    <div
                        style={{
                            marginBlockEnd: 24,
                        }}
                    >
                        <ProFormCheckbox noStyle name="autoLogin">
                            自动登录
                        </ProFormCheckbox>
                        <a
                            style={{
                                float: 'right',
                            }}
                        >
                            忘记密码
                        </a>
                    </div>
                </LoginForm>
            </div>
        </ProConfigProvider>
    )

}

export default LoginComponent

