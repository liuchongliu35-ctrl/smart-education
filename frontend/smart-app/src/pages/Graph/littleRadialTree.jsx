import React, { useRef, useState } from 'react';
import ForceGraph2D from 'react-force-graph-2d';
import { Avatar, Button, Card, Drawer, Input, List, Space, Typography } from 'antd';




const { Title, Text } = Typography;

const LittleKnowledgeGraph = ({ data }) => {
    const graphRef = useRef();
    const [selectedNode, setSelectedNode] = useState('')
    const [showDetails, setShowDetails] = useState(false)
    const [hoverNode, setHoverNode] = useState(null)


    const handleHover = (node) => {
        setHoverNode(node)
    }

    // 节点点击处理函数
    const handleNodeClick = (node) => {
        setSelectedNode(node);
        setShowDetails(true);
    };

    // 背景点击处理函数
    const handleBackgroundClick = () => {
        setShowDetails(false);
    };

    // 根据节点层级计算半径大小（level越大半径越小）
    const calculateNodeRadius = (level) => {
        // 进一步减小节点尺寸
        const baseSize = 15;
        const sizeMultiplier = 0.8;
        return baseSize * Math.pow(sizeMultiplier, level + 2);
    };

    // 根据层级获取节点颜色
    const getNodeColor = (level) => {
        const colors = {
            1: '#45d0bd',
            2: '#fb8c00',
            3: '#a2b1fb'
        }
        return colors[level] || '#76b7b2'; // 默认颜色
    }
    const getNodeColor2 = (level) => {
        const colors = {
            1: '#79dacd',
            2: '#f8b96c',
            3: '#c0caf3'
        }
        return colors[level] || '#76b7b2'; // 默认颜色
    }
    const getHoverNodeColor = (level) => {
        const colors = {
            1: '#007a6a',
            2: '#e65100',
            3: '#7472fe'
        };
        return colors[level] || '#76b7b2'; // 默认颜色
    }
    const getHoverNodeColor2 = (level) => {
        const colors = {
            1: '#33b6a4',
            2: '#f87027',
            3: '#a19fff'
        };
        return colors[level] || '#76b7b2'; // 默认颜色
    }

    return (
        <div style={{
            position: 'relative',
            width: '100%',
            height: '100%',
            background: '#F6F7FB',
            borderRadius: '8px',
            boxShadow: '0 4px 20px rgba(0, 0, 0, 0.08)',
            overflow: 'hidden'
        }}>
            <ForceGraph2D
                ref={graphRef}
                graphData={data}
                nodeAutoColorBy="level"
                nodeVal={node => calculateNodeRadius(node.level)}
                linkCurvature={0.05} // 减小曲线弧度
                linkColor={() => 'rgba(120, 120, 120, 0.25)'} // 更淡的连线颜色
                linkWidth={1.5} // 更细的连线
                d3VelocityDecay={0.5} // 降低速度衰减，使布局更稳定
                cooldownTicks={100} // 延长冷却时间
                linkDistance={link => {
                    // 根据节点层级动态设置链接长度
                    const sourceLevel = link.source.level || 1;
                    const targetLevel = link.target.level || 1;

                    // 层级差越大，链接越长
                    return 100 + Math.abs(sourceLevel - targetLevel) * 400;
                }}
                onNodeHover={(node) => {
                    handleHover(node) // 更新悬停节点
                }}

                nodeCanvasObject={(node, ctx, globalScale) => {
                    const radius = calculateNodeRadius(node.level);
                    const maxWidth = radius * 4; // 最大文字宽度
                    const ellipsis = "...";
                    // const label = node.topTitle;
                    const fontSize = Math.min(10, radius * 0.7);



                    // 确保节点位置是有效数字
                    if (!isFinite(node.x)) return;
                    if (!isFinite(node.y)) return;

                    // 绘制节点主体
                    ctx.beginPath();
                    ctx.arc(node.x, node.y, radius, 0, 2 * Math.PI, false);

                    // 创建球形渐变效果
                    const gradient = ctx.createRadialGradient(
                        node.x - radius / 3, node.y - radius / 3, radius / 10,
                        node.x, node.y, radius
                    );
                    const baseColor = getNodeColor(node.level)
                    const gradientColor = getNodeColor2(node.level)

                    gradient.addColorStop(0, '#ffffff');
                    gradient.addColorStop(0.5, gradientColor)
                    gradient.addColorStop(1, baseColor)

                    const hoverBaseColor = getHoverNodeColor(node.level)
                    const gradientHoverColor = getHoverNodeColor2(node.level)

                    const gradientHover = ctx.createRadialGradient(
                        node.x - radius / 3, node.y - radius / 3, radius / 10,
                        node.x, node.y, radius
                    );

                    gradientHover.addColorStop(0, '#ffffff');
                    gradientHover.addColorStop(0.7, gradientHoverColor);
                    gradientHover.addColorStop(1, hoverBaseColor);



                    ctx.fillStyle = node === hoverNode
                        ? gradientHover
                        : gradient;
                    // ctx.fillStyle = baseColor
                    ctx.fill();

                    // 测量文字宽度
                    ctx.font = `${fontSize}px Sans-Serif`;
                    const fullText = node.topTitle;
                    let displayText = fullText;

                    // 如果文字过长，截断并添加 ...
                    if (ctx.measureText(fullText).width > maxWidth) {
                        let truncated = "";
                        for (let i = 0; i < fullText.length; i++) {
                            truncated = fullText.substring(0, i);
                            if (ctx.measureText(truncated + ellipsis).width > maxWidth) {
                                displayText = truncated.slice(0, -1) + ellipsis;
                                break;
                            }
                        }
                    }


                    ctx.textAlign = 'center'
                    ctx.textBaseline = 'top'
                    ctx.fillStyle = '#444'
                    ctx.fillText(displayText, node.x, node.y + radius + 5);
                }}
                nodePointerAreaPaint={(node, color, ctx) => {
                    const radius = calculateNodeRadius(node.level);

                    // 确保节点位置是有效数字
                    if (!isFinite(node.x)) return;
                    if (!isFinite(node.y)) return;

                    ctx.fillStyle = color;
                    ctx.beginPath();
                    ctx.arc(node.x, node.y, radius, 0, 1 * Math.PI);
                    ctx.fill();
                }}
                onNodeClick={handleNodeClick}
                onBackgroundClick={handleBackgroundClick}
                // 添加布局稳定回调
                onEngineStop={() => {
                    graphRef.current.zoom(5, 600);
                }}
            />



        </div>
    );
};

export default LittleKnowledgeGraph;


