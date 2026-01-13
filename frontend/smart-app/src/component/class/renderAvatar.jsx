import { TeamOutlined } from '@ant-design/icons';
import { Avatar } from 'antd'

export function renderAvatar(e) {
  switch (e) {
    case 3:
      return <Avatar size={42} icon={<TeamOutlined />} style={{ marginRight: 16, background: '#f56a00' }} />
    case 4:
      return <Avatar size={42} icon={<TeamOutlined />} style={{ marginRight: 16, background: '#283be5' }} />
    default:
      return <Avatar size={42} icon={<TeamOutlined />} style={{ marginRight: 16, background: '#444444' }} />
  }
}
export function renderClassListAvatar(e) {
  switch (e) {
    case 3:
      return <Avatar size={64} icon={<TeamOutlined />} style={{ background: '#f56a00' }} />
    case 4:
      return <Avatar size={64} icon={<TeamOutlined />} style={{ background: '#283be5' }} />
    default:
      return <Avatar size={64} icon={<TeamOutlined />} style={{ background: '#444444' }} />
  }
}
