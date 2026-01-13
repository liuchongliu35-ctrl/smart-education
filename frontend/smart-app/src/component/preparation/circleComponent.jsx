import React, { useState } from 'react';


const CircleComponent = ({size}) => {
  const [isHovered, setIsHovered] = useState(false);
  
  // 圆形参数
  const radius = size // 半径
  const circumference = 2 * Math.PI * radius; // 周长 = 2πr

  const initialDash =  0; // 可见部分
  const initialGap = circumference - initialDash; // 不可见部分

  return (
    <div
      style={{ cursor: 'pointer',width:'max-content' }}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
    >
      <svg width={2*size} height={2*size} style={{borderRadius:'50%'}}>
        <circle
          cx="20"
          cy="20"
          r={radius}
          fill='none'
          stroke="#db3474"
          strokeWidth="8"
          strokeLinecap="round"
          strokeDasharray={
            isHovered 
              ? `${circumference} 0` // 100% 显示（无间隙）
              : `${initialDash} ${initialGap}` // 40% 显示
          }
          style={{
            transition: '1s', // 动画效果
          }}
        />
      </svg>
    </div>
  );
};

export default CircleComponent