import React, { useEffect, useState } from 'react';
import { Button, Checkbox, Col, ConfigProvider, Divider, Form, Input, Rate, Row, Cascader } from 'antd';
import style from '@/pages/homeWork/homeWork.module.css'
import ModalLoadingComponent from '../Loading/modalLoading';


const { TextArea } = Input

const difficultyItem = ['简单', '正常', '进阶']
const workTypeItem = ['单选题', '多选题', '填空题', '简答题']

const WorkSelectForm = ({ onSubmit, allData }) => {
  const [knowledgeData, setKnowledgeData] = useState([])
  const [form] = Form.useForm()
  const uid = sessionStorage.getItem('uid')
  //章节知识点
  useEffect(() => {
    if (allData) {
      setKnowledgeData(allData)
    }
  }, [])

  const handleSubmit = (values) => {
    const { ptitle, ...restValues } = values
    const prop = {
      ...restValues,
      problemType: values.problemType?.join(',') || '',
      difficulty: values.difficulty?.join(',') || '',
      score: parseInt(values.score, 10) || 0,
      quantity: parseInt(values.quantity, 10) || 0,
      htype: 0,
      htitle: values.ptitle[0],
      secondaryTitle: values.ptitle[1],
    }
    console.log(prop)
    if (onSubmit) {
      onSubmit(prop, uid)
    }
  }


  return (


    <ConfigProvider>
      <Form
        onFinish={handleSubmit}
        layout="vertical"
        labelCol={{ flex: '30px' }}
        labelAlign="left"
        wrapperCol={{ flex: 0.5 }}
        colon={false}
        style={{ width: 320 }}
        form={form}

      >
        <Form.Item label="知识点" name="ptitle" rules={[{ required: true, message: '知识点未选择！' }]}>

          <Cascader
            fieldNames={{ label: 'title', value: 'title', children: 'children' }}
            options={knowledgeData}
            placeholder="请选择知识点"
          />
        </Form.Item>

        <Row gutter={8}>
          <Col span={12}>
            <Form.Item
              name="problemType"
              label="类型"
              rules={[{ required: true, message: '题型未选择！' }]}
            >
              <Checkbox.Group options={workTypeItem} style={{ marginLeft: 0 }} />
            </Form.Item>
          </Col>
          <Col span={2}></Col>
          <Col span={10}>
            <Form.Item name="difficulty" label="难度" rules={[{ required: true, message: '难度未选择！' }]}>
              <Checkbox.Group options={difficultyItem} style={{ marginLeft: 0 }} />
            </Form.Item>
          </Col>
        </Row>

        <Row gutter={8}>
          <Col span={12}>
            <Form.Item label="题目数量" name="quantity" rules={[{ required: true, message: '题目数量未确定！' }]}>
              <Input
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
            <Form.Item label="作业总分" name="score" rules={[{ required: true, message: '作业总分未确定！' }]}>
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



        <Form.Item label="作业名称" name="hname" rules={[{ required: true, message: '请输入作业名称！' }]}>
          <Input />
        </Form.Item>

        <Form.Item label="作业说明" name="explanation">
          <TextArea />
        </Form.Item>

        <div style={{ width: 'max-content', margin: 'auto' }}>
          <button
            className={style.functionBtn}
            type="primary"
            htmlType="submit"
            style={{ height: 30, width: 200 }}
          >
            点击创建
          </button>
        </div>


      </Form>

    </ConfigProvider>

  )
}
export default WorkSelectForm