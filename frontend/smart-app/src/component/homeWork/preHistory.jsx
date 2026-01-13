import style from '@/pages/homeWork/homeWork.module.css'
import { RestOutlined } from '@ant-design/icons';
import { Avatar, Pagination } from 'antd'
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom'
import NoneDataIcon from '@/assets/svg/暂无内容.svg'
import { getPreList } from '../../apis/homeworkAPI'
import { renderAvatar } from '../class/renderAvatar'

const PreHistory = ({ onPublish, reload }) => {
  const [preList, setPreList] = useState([])
  const navigate = useNavigate()
  const uid = sessionStorage.getItem('uid')

  //数据请求
  const getList = async () => {
    let { data } = await getPreList(uid)
    setPreList(data)
  }
  useEffect(() => {
    getList()
  }, [reload])


  /* ——————分页逻辑—————— */
  const [currentPage, setCurrentPage] = useState(1)
  const [pageSize, setPageSize] = useState(10)

  // 计算当前页数据
  const currentData = preList?.slice(
    (currentPage - 1) * pageSize,
    currentPage * pageSize
  )
  /* ——————分行—————— */

  function renderTag(e) {
    switch (e) {
      case 0:
        return <span className={style.noTag}>未发布</span>;
      case 1:
        return <span className={style.workTag}>进行中</span>;
      case -1:
        return <span className={style.falseTag}>已结束</span>;

      default:
        break;
    }
  }



  return (
    <>
      {currentData && currentData.length > 0 ? (
        <>
          {currentData?.map((item, index) => (
            <div className={style.workHistoryItem} key={index}>
              <span style={{ fontFamily: 'siyuan', fontSize: 18, width: 240, textAlign: 'left' }}>{item.previewName}</span>
              <span >{renderTag(item.active)} </span>
              <span style={{ fontWeight: 600, color: '#808080', width: 40 }}>{item.questionsGrade}分</span>
              <span style={{ fontWeight: 600, color: '#808080', width: 50 }}>{item.questionsNum}题</span>
              {item.active === 0 ? <span>创建时间：{item.createTime}</span> : <span>截止时间: {item.deadline}</span>}
              <div style={{ width: 160, textAlign: "left" }}>
                {item.active !== 0 && (
                  <div>
                    {renderAvatar(item.cid)}
                    <span>{item.className}</span>
                  </div>
                )
                }
              </div>
              <div style={{ width: 180, display: 'flex' }}>
                {item.active === 0 && <button className={style.functionBtn} onClick={() => onPublish(item.ptId)}>发布</button>}
                <div style={{ flex: 1 }}> </div>
                <button className={style.detalBtn} onClick={() => navigate(`/preview/${item.ptId}`)}>查看作业</button>
                <div style={{ width: 50, display: 'flex', justifyContent: 'center', alignItems: 'center', color: '#fd5252' }}><RestOutlined /></div>
              </div>
            </div>
          ))}
          <Pagination
            current={currentPage}
            pageSize={pageSize}
            total={preList.length}
            align='center'
            onChange={(page, size) => {
              setCurrentPage(page)
              setPageSize(size);
            }}
            style={{
              marginTop: 20,
              textAlign: 'center'
            }}
          />
        </>)
        : (
          <div>
            <Avatar src={NoneDataIcon} size={128} />
            <div style={{ fontFamily: 'youshe', color: '#000', fontSize: 22 }}>暂无数据</div>
          </div>
        )}
    </>
  )
}

export default PreHistory