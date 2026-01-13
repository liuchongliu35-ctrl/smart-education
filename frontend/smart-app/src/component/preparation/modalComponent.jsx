import React, { useEffect, useState } from 'react'
import { Cascader, Col, Form, Input, Row, Select, } from 'antd'
import style from '../../pages/preparationBoard/preparationBoard.module.css'
import { postCreateNewDesign } from '@/apis/preparation'
import { getSubjectList } from '../../apis/homeworkAPI'
import { useParams } from 'react-router-dom'


const ModalComponent = ({ onSubmit, initFormData }) => {
  let { tsId, schoolId } = useParams()
  const [knowledgeData, setKnowledgeData] = useState([])
  const [form] = Form.useForm()
  const uid = sessionStorage.getItem('uid')

  const getSubjectItem = async () => {
    let { data } = await getSubjectList(tsId, schoolId)
    setKnowledgeData(data)
    console.log(initFormData)
    form.setFieldsValue(initFormData)
  }

  useEffect(() => {
    getSubjectItem()
  }, [])



  const handleSubmit = (values) => {
    const prop = {
      ...values,
      authorId: uid,
      designTitle: values.ptitle[0],
      secondaryTitle: values.ptitle[1],
    }
    if (onSubmit) {
      onSubmit(prop)
      console.log(prop)
    }
  }


  return (
    <Form
      onFinish={handleSubmit}
      layout="vertical"
      labelCol={{ flex: '30px' }}
      labelAlign="left"
      colon={false}
      style={{ width: 240, marginTop: 50 }}
      form={form}

    >
      {/* 知识点 */}
      <Form.Item label="知识点" name="ptitle" rules={[{ required: true, message: '知识点未选择！' }]}>

        <Cascader
          fieldNames={{ label: 'title', value: 'title', children: 'children' }}
          options={knowledgeData}
          placeholder="请选择知识点"
        />
      </Form.Item>

      <Row gutter={8}>
        <Col span={10}>
          <Form.Item
            name="classTime"
            label="	课时数"
            rules={[{ required: true, message: '题型未选择！' }]}
          >
            <Input
              style={{
                height: 28,
                padding: '2px 8px',
                fontSize: 13
              }}
            />
          </Form.Item>
        </Col>
        <Col span={4}></Col>
        <Col span={10}>
          <Form.Item name="subject" label="学科" rules={[{ required: true, message: '难度未选择！' }]}>
            <Input
              style={{
                height: 28,
                padding: '2px 8px',
                fontSize: 13
              }}
            />
          </Form.Item>
        </Col>
      </Row>
      <Form.Item label="授课对象" name="target" rules={[{ required: true, message: '名称未填写！' }]}>
        <Input style={{ height: 28, padding: '2px 8px', fontSize: 13 }} />
      </Form.Item>
      <Form.Item label="教学设计名称" name="designName" rules={[{ required: true, message: '名称未填写！' }]}>
        <Input
          placeholder="请输入教学设计名称"
        />
      </Form.Item>

      <Form.Item >
        <div style={{ textAlign: 'center' }}>
          <button
            htmlType="submit"
            className={style.shengchengBtn}
          >
            下一步
          </button>
        </div>
      </Form.Item>


    </Form>
  )
}
export default ModalComponent