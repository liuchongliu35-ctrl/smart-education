import * as echarts from 'echarts';
import React from 'react';
import ReactEcharts from 'echarts-for-react';
var colorList = ['#435fff', '#73ACFF', '#fa5537']

const PassPie = ({passRate}) => {
    const goodRate = passRate[0]
    const passingRate = passRate[1]
    const unPassingRate = passRate[2]
const option = {
        title: {
            x: 'center',
            y: 'center',
            textStyle: {
                fontSize: 20
            }
        },
        tooltip: {
            trigger: 'item'
        },
        series: [{
            type: 'pie',
            center: ['50%', '50%'],
            radius: ['5%', '45%'],
            clockwise: true,
            avoidLabelOverlap: true,
            hoverOffset: 15,
            itemStyle: {
                normal: {
                    color: function(params) {
                        return colorList[params.dataIndex]
                    }
                }
            },
            label: {
                show: true,
                position: 'outside',
                formatter: '{a|{b}：{d}%}\n{hr|}',
                rich: {
                    hr: {
                        
                        borderRadius:3,
                        width: 2,
                        height: 2,
                        padding: [3, 3, 0, -12]
                    },
                    a: {
                        padding: [-15,0, -20, 5]
                    }
                }
            },
            labelLine: {
                normal: {
                    length: 10,
                    length2: 10,
                    lineStyle: {
                        width: 1
                    }
                }
            },
            data: [{
                'name': '优秀',
                'value': goodRate
            }, {
                'name': '合格',
                'value': passingRate
            }, {
                'name': '不合格',
                'value': unPassingRate
            }
            ],
        }]
    }

  return <ReactEcharts option={option} />
}

export default PassPie

