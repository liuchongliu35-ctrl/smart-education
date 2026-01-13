import * as echarts from 'echarts';
import React from 'react';
import ReactEcharts from 'echarts-for-react';

var color = ['#FC4567', '#3240D5', '#8674FE','#FF8965']

const option = {
    color: color,
    title: {
        left: 'center',
        top: '60%',
        textStyle: {
            fontSize: 22,
            color: '#000',
            fontWeight: 'normal'
        }
    },
    tooltip: {
        trigger: 'item'
    },
    legend: {
        left:'center',
        bottom:5
    },
    series: [{
        type: 'pie',
        roseType: 'radius',
        radius: ['30%', '50%'],
        data: [{
                value: 220,
                name: '单选题'
            }, {
                value: 120,
                name: '多选题'
            },
            {
                value: 189,
                name: '填空题'
            },
            {
                value:99,
                name:'简答题'
            }
        ],
        label: {
            normal: {
                formatter: '{font|{d}%}',
                rich: {
                    font: {
                        fontSize: 14,
                        padding: 3,
                        color: '#535353',
                        fontFamily:'siyuan'
                    },
                    hr: {
                        height: 0,
                        borderWidth: 1,
                        width: '100%',
                        borderColor: '#535353'
                    }
                }
            },
        },
        labelLine: {
            lineStyle: {
                color: '#535353'
            }
        },
        emphasis: {
            itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0,0,0,0.5)'
            }
        }
    }]
};




const WrongPie = () => {


  return <ReactEcharts option={option} />
}

export default WrongPie

