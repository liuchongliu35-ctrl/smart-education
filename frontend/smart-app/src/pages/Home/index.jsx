import React, { useEffect, useState } from 'react'
import { Layout, ConfigProvider, Avatar, Skeleton, Menu, Badge, Tag, Popover } from 'antd'
import style from './home.module.css'
import Icon, { SearchOutlined, HomeOutlined, FormOutlined, TeamOutlined, ShopOutlined, CalendarOutlined } from '@ant-design/icons';
import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import TeacherIcon from '../../assets/svg/白领男士男人.svg'
import { getUserInfo, postQuitUser } from '../../apis/user'

const items = [
  {
    key: '1',
    icon: <HomeOutlined />,
  },
  {
    key: '2',
    icon: <FormOutlined />,
  }, {
    key: '3',
    icon: <CalendarOutlined />
  }, {
    key: '4',
    icon: <TeamOutlined />,
  }

]
const { Sider, Content } = Layout

const contentStyle = {
  minWidth: 700,
  textAlign: 'center',
  minHeight: '100vh',
  lineHeight: '120px',
  color: '#fff',
  backgroundColor: '#F2F5FF',
  position: 'reletive',
  marginLeft: 80
}

const siderStyle = {
  height: '100vh',
  position: 'fixed',
  textAlign: 'center',
  background: '#fff',
  boxShadow: '0px 0px 2px #0000002f'
}

const layoutStyle = {
  borderRadius: 8,
  overflow: 'hidden',
  minWidth: 1000,
  backgroundColor: '#fff '
}




const HomePage = () => {
  // 配置主题参数
  const ThemeConfig = {

    components: {
      Input: {
        activeBg: '#fff',
        activeBorderColor: '#435fff',    // 聚焦时边框
        hoverBorderColor: '#435fff',     // 悬停时边框
        paddingBlock: 10,                // 纵向间距
        paddingInline: 0,               // 横向间距
        activeShadow: '0 0 0 2px #435fff26', // 聚焦阴影
      },
      Menu: {
        iconSize: 26,
        iconMarginInlineEnd: 0,
        itemHeight: 60,
        itemMarginInline: 0,
        activeBarWidth: 5,
        activeBarColor: '#3b3c50',
        itemSelectedColor: '#0B2273',
        itemBorderRadius: 0,
        itemSelectedBg: '#a4abd644',
        itemBg: '#F8FAF9'
      },
    }
  }


  /*————Router控制区域—————*/
  const navigate = useNavigate()
  const location = useLocation()
  const [tsId, setTsId] = useState('')
  const [schooldId, setSchoolId] = useState('')
  const currentPaths = location.pathname
  const [currentPath, setCurrentPath] = useState(sessionStorage.getItem('currentShow'))
  const [name, setName] = useState('')
  const [teachStage, setTeachStage] = useState('')
  const [userName, setUserName] = useState('')

  function judgeRoute(e) {
    switch (e) {
      case '/home':
        setCurrentPath('1')
        sessionStorage.setItem('currentShow', '1')
        break;
      case `/home/preparation/${tsId}/${schooldId}`:
        setCurrentPath('2')
        sessionStorage.setItem('currentShow', '2')
        break;
      case `/home/homeworkentry/${tsId}/${schooldId}`:
        setCurrentPath('3')
        sessionStorage.setItem('currentShow', '3')
        break;
      case '/home/classentry':
        setCurrentPath('4')
        sessionStorage.setItem('currentShow', '4')
        break;
      case '/home/preparationhistory':
        setCurrentPath('2')
        sessionStorage.setItem('currentShow', '2')
        break;
      // case '/home':
      //   setCurrentPath('1')
      //   break;
      default:
        break;
    }
  }

  const getInfo = async () => {
    const { data } = await getUserInfo()
    setName(data.name)
    setTeachStage(data.teachStage)
    sessionStorage.setItem('uid', data.uid)
    setUserName(data.account)
    setTsId(data.tsId)
    setSchoolId(data.schoolId)
  }

  const handleQuit = async () => {
    await postQuitUser()
    localStorage.removeItem('authToken')
    navigate('/', { state: { showQuitSuccess: true } })
  }

  useEffect(() => {
    getInfo()
    judgeRoute(currentPaths)
  }, [currentPaths])

  const menuOnClick = (e) => {
    // navigate(`${e.key}`)
    switch (e.key) {
      case '1':
        navigate('/home')
        setCurrentPath('1')
        break;
      case '2':
        navigate(`/home/preparation/${tsId}/${schooldId}`)
        setCurrentPath('2')
        break;
      case '3':
        navigate(`/home/homeworkentry/${tsId}/${schooldId}`)
        setCurrentPath('3')
        break;
      case '4':
        navigate('/home/classentry')
        setCurrentPath('4')
        break;
      default:
        break;
    }
  }

  const handleGoInfo = () => {
    navigate('/home/user')
    setCurrentPath('0')
  }

  /*———分行—————*/
  const content = () => (
    <div style={{ paddingInline: 5, textAlign: 'center' }}>
      <div style={{ paddingBlock: 5, borderBottom: '2px solid #eaeaea' }}>{userName} , 您好！</div>
      <div>
        <div className={style.popBtn} onClick={() => handleGoInfo()}>个人中心</div>
        <div className={style.popBtn} onClick={() => handleQuit()} style={{ color: '#fd5252' }}>退出登录</div>

      </div>
    </div>
  )




  return (
    <ConfigProvider theme={ThemeConfig}

    >
      <Layout style={layoutStyle}>
        <Sider width={80} className={style.Sider}>
          <div style={{ width: '100%', height: 72, paddingInline: 10 }} className={style.generation}>
            <span style={{ fontFamily: 'youshe', color: '#435fff', fontSize: 24, lineHeight: 1 }}>小智备课</span>
          </div>

          <Menu
            onClick={menuOnClick}
            style={{
              width: '100%',
              marginTop: '180%',
            }}
            // defaultSelectedKeys={['1']}
            mode="inline"
            items={items}
            selectedKeys={[`${currentPath}`]}
          />
          <div style={{ width: '100%', position: 'absolute', bottom: 100, display: 'flex', flexDirection: 'column', alignItems: 'center' }} >
            <Popover placement="rightTop" content={content}>
              <Avatar className={style.avatarStyle} src={TeacherIcon} size={46} />
            </Popover>
            <Tag color="gold" style={{ marginRight: 0, marginTop: 8, fontSize: 11 }}>{teachStage}</Tag>
            <div style={{ fontFamily: 'siyuan', fontSize: 13, marginTop: 5, color: '#414141' }}>{name}</div>
          </div>
        </Sider>
        <Layout style={{ backgroundColor: '#fff' }}>


          <Content style={contentStyle} className={style.content}>
            <Outlet />
          </Content>
          {/* <Footer style={footerStyle}>Footer</Footer> */}
        </Layout>
      </Layout>
    </ConfigProvider>
  )
}
export default HomePage