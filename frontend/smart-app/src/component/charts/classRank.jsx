import * as echarts from 'echarts';
import React from 'react';
import ReactEcharts from 'echarts-for-react';

const option = {
  color: ['#7e80e9'],

  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'cross',
      label: {
        backgroundColor: '#9294E9'
      }
    }
  },
  legend: {
    data: ['名次'],
    right: 100
  },
  title: {
    left: 40,
    text: '考试排名',
    color: '#031027'
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: [
    {
      type: 'category',
      boundaryGap: false,
      data: ['第三次考试','第四次考试','第五次考试','第六次考试','第七次考试','第八次考试','第九次考试']
    }
  ],
  yAxis: [
    {
      type: 'value'
    }
  ],
  series: [
    {
      showAllSymbol: true,
      label: {
        show: true,
        position: 'top'
      },
      name: '名次',
      type: 'line',
      stack: 'Total',
      smooth: true,
      lineStyle: {
        width: 2
      },
      showSymbol: false,
      areaStyle: {
        opacity: 0.5,
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          {
            offset: 0,
            color: '#6d6fe7'
          },
          {
            offset: 1,
            color: '#094bbc32'
          }
        ])
      },
      emphasis: {
        focus: 'series'
      },
      data: [11,12,8,9,3,7,10]
    },
  ]
};




const ClassRankLine = () => {


  return <ReactEcharts option={option} />
}

export default ClassRankLine

