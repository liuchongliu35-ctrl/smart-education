import { Avatar } from 'antd'
import NoneData from '../../assets/svg/暂无数据.svg'





const NonePage = () => {
    return (
        <div style={{ width: '100%', height: '100vh', display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
            <Avatar src={NoneData} size={400} />
            <div style={{ fontFamily: 'youshe', fontSize: 26 }}>出错了，请返回<a href='/home'>首页</a></div>

        </div>
    )
}

export default NonePage