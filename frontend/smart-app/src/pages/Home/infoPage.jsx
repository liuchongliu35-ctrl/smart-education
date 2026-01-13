import { useNavigate } from 'react-router-dom'
import { useEffect, useState } from 'react'
import style from './home.module.css'
import { Card, Form, Input, Upload, Button, message, Avatar, Row, Col, Modal } from 'antd';
import { UserOutlined, CameraOutlined, SaveOutlined } from '@ant-design/icons';
import ManAvatar from '../../assets/png/白领男士男人.png'
import { getUserInfo, modifyUserInfo, postAddschool } from '../../apis/user';


const UserInfoPage = () => {

    const navigate = useNavigate()
    const [form] = Form.useForm()
    const [avatarUrl, setAvatarUrl] = useState('')
    const [loading, setLoading] = useState(false)
    const [addLoading, setAddLoading] = useState(false)
    const [initialValue, setInitialValue] = useState({})
    const [open, setOpen] = useState(false)
    const [record, setRecord] = useState(true)
    const [messageApi, contextHolder] = message.useMessage()

    const getInitialInfo = async () => {
        const { data } = await getUserInfo()
        console.log(data)
        setInitialValue(data)
        form.setFieldsValue(data)
    }

    useEffect(() => {
        getInitialInfo()
    }, [record])

    const showModal = (e) => {
        setTimeout(() => {
            setOpen(true)
        }, 50)
    }

    const handleCancel = () => {
        setOpen(false)
    }

    // 处理头像上传
    const handleAvatarUpload = ({ file }) => {
        if (file.status === 'done') {
            // 模拟上传成功后的处理
            const reader = new FileReader()
            reader.onload = (e) => {
                setAvatarUrl(e.target.result)
                message.success('头像上传成功')
            };
            reader.readAsDataURL(file.originFileObj)
        }
    };

    // 处理表单提交
    const handleSubmit = async (values) => {
        setLoading(true)
        await modifyUserInfo(values)
        setTimeout(() => {
            messageApi.open({
                type: 'success',
                content: '个人信息更新成功！',
            })
            setLoading(false)
        }, 1500)
    }

    const handleAddSchool = async (values) => {
        setAddLoading(true)
        await postAddschool(values)
        setTimeout(() => {
            message.success('添加学校成功')
            setOpen(false)
            setAddLoading(false)
            setRecord(pre => !pre)
        }, 1500)
    }

    // 上传前验证
    const beforeUpload = (file) => {
        const isImage = file.type.startsWith('image/');
        if (!isImage) {
            message.error('请上传图片文件');
        }
        const isLt2M = file.size / 1024 / 1024 < 2;
        if (!isLt2M) {
            message.error('图片大小不能超过2MB')
        }
        return isImage && isLt2M;
    }



    return (
        <div style={{ width: '100%', padding: 20, height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            {contextHolder}
            <div style={{ width: '40%', minHeight: 680, background: '#fff', borderRadius: 5, boxShadow: '0px 0px 6px #0000002f' }}>
                <div style={{ color: '#000', width: '100%', height: 100, lineHeight: '100px', textAlign: 'center', fontSize: 30, fontWeight: 600, borderBottom: '1px solid #000' }}>
                    个人信息</div>
                <div style={{ marginTop: 50, paddingInline: 100 }}>
                    <Form
                        style={{ width: '100%' }}
                        form={form}
                        onFinish={handleSubmit}
                        labelCol={{ span: 5, style: { textAlign: 'right' } }}
                    >
                        <div style={{ marginBottom: 24, display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
                            <Avatar
                                size={78}
                                src={ManAvatar}
                                icon={<UserOutlined />}
                                style={{ marginBottom: 16 }}
                            />
                            <Upload
                                name="avatar"
                                showUploadList={false}
                                beforeUpload={beforeUpload}
                                customRequest={handleAvatarUpload}
                                accept="image/*"
                            >
                                <Button
                                    type="primary"
                                    icon={<CameraOutlined />}
                                    style={{ width: 150 }}
                                >
                                    更换头像
                                </Button>
                            </Upload>
                            <p style={{ marginTop: 8, color: '#999' }}>
                                支持 JPG, PNG 格式，大小不超过 2MB
                            </p>
                        </div>
                        <Form.Item
                            label="用户名"
                            name="account"
                        >
                            <Input placeholder="请输入用户名" style={{ paddingInline: 15 }} />
                        </Form.Item>

                        <Form.Item
                            label="姓名"
                            name="name"
                        >
                            <Input placeholder="请输入姓名" style={{ paddingInline: 15 }} />
                        </Form.Item>


                        <Form.Item
                            label="工号"
                            name="uid"
                        >
                            <Input disabled placeholder="请输入学校名称" style={{ paddingInline: 15 }} />
                        </Form.Item>

                        <Form.Item
                            label="电话号码"
                            name="phone"
                        >
                            <Input placeholder="请输入电话号码" style={{ paddingInline: 15 }} />
                        </Form.Item>

                        <Form.Item
                            label="邮箱"
                            name="email"
                        >
                            <Input placeholder="请输入邮箱" style={{ paddingInline: 15 }} />
                        </Form.Item>


                        <Form.Item
                            label="学校"
                            name="schoolName"
                        >
                            {initialValue?.schoolName === null ? (<div style={{ display: 'flex', alignItems: 'center', paddingInline: 15 }}>
                                <Input
                                    disabled
                                    placeholder="请关联学校"
                                    style={{ paddingInline: 15, flex: 1, marginRight: 8 }}
                                />
                                <Button type="primary" onClick={() => showModal()}>关联学校</Button>
                            </div>

                            ) :
                                <Input
                                    disabled
                                    placeholder="请关联学校"
                                    style={{ paddingInline: 15 }}
                                />
                            }
                        </Form.Item>
                        <Form.Item style={{ marginTop: 32 }}>
                            <Button
                                type="primary"
                                htmlType="submit"
                                icon={<SaveOutlined />}
                                loading={loading}
                                style={{ width: 150, height: 40 }}
                            >
                                保存修改
                            </Button>
                        </Form.Item>
                    </Form>


                </div>

            </div>
            <Modal
                width={600}
                open={open}
                destroyOnClose={true}
                cancelButtonProps={{ style: { display: 'none' } }}
                okButtonProps={{ style: { display: 'none' } }}
                onCancel={handleCancel}
            >
                <div style={{ fontSize: 22, width: '80%', textAlign: 'center', paddingBottom: 10, fontWeight: 600, margin: 'auto', marginBottom: 10 }}
                >关联学校</div>
                <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', width: 500, margin: 'auto' }}>
                    <Form
                        onFinish={handleAddSchool}
                        style={{ width: '60%' }}
                        layout="vertical">

                        <Form.Item
                            label="学校"
                            name="schoolName"
                            rules={[{ required: true, message: '请输入学校名称' }]}
                        >
                            <Input placeholder="请输入学校名称" />
                        </Form.Item>

                        <Form.Item
                            label="学校地址"
                            name="address"
                            rules={[{ required: true, message: '请输入学校地址' }]}
                        >
                            <Input placeholder="请输入学校地址" />
                        </Form.Item>

                        <Form.Item
                            label="联系人"
                            name="contact"
                            rules={[{ required: true, message: '请输入联系人姓名' }]}
                        >
                            <Input placeholder="请输入联系人姓名" />
                        </Form.Item>

                        <Form.Item
                            label="联系人号码"
                            name="contactPhone"
                            rules={[{ required: true, message: '请输入联系人号码' }]}
                        >
                            <Input placeholder="请输入联系人号码" />
                        </Form.Item>

                        <Form.Item style={{ marginTop: 32 }}>
                            <div style={{ width: 'max-content', margin: 'auto' }}>
                                <Button
                                    loading={addLoading}
                                    type="primary"
                                    htmlType="submit"
                                    style={{ width: 120, height: 40 }}
                                >
                                    添加学校
                                </Button>
                            </div>
                        </Form.Item>

                    </Form>

                </div>
            </Modal>

        </div>
    )
}
export default UserInfoPage
