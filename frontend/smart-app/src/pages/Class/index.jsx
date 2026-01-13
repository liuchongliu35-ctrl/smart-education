import {ConfigProvider} from "antd"
import { Outlet, useNavigate } from "react-router-dom"
import style from './class.module.css'
import Icon, {LeftOutlined} from '@ant-design/icons';


const ClassPage = () => {

      /* ———————通用——————— */
      const navigate = useNavigate()

      /* ———————分行——————— */

    return (
        <ConfigProvider
        theme={{
          components: {
            Statistic: {
              contentFontSize:24,
              titleFontSize:13
            },
          },
        }}
          >
        <div style={{minHeight:'100vh',background:'#F3F9FE',minWidth:1500}}>
   
    
         <Outlet />
     
        </div>  
        </ConfigProvider>
    )
  }
  export default ClassPage