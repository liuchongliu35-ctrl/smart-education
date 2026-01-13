import KnowledgeGraph from "./radialTree"
import testData from './knowledgData.json'
import { getGraphData } from "../../apis/preparation"
import { useEffect, useState } from "react"

const defaultData = {
    nodes: [
        {
            "id": 1057,
            "source": "custom",
            "topTitle": "第七讲 人工智能前沿技术",
            "secondaryTitle": null,
            "content": "掌握人工智能领域最新技术发展趋势与应用前景",
            "level": 2,
            "schoolId": 4,
            "tsId": 11,
            "templateId": null,
            "children": [
                {
                    "id": 1063,
                    "source": "custom",
                    "topTitle": "量子机器学习导论",
                    "secondaryTitle": "第七讲",
                    "content": "量子计算基础、量子神经网络原理与混合量子-经典算法",
                    "level": 3,
                    "schoolId": 4,
                    "tsId": 11,
                    "templateId": null,
                    "children": null,
                    "parents": []
                }
            ],
            parents: []
        }
    ],
    links: [
        {
            "source": 1071,
            "target": 1080,
            "relationId": 755,
            "relationDesc": "知识表示是专家系统的基础"
        },
        {
            "source": 1072,
            "target": 1076,
            "relationId": 756,
            "relationDesc": "搜索算法是计算思维的应用"
        }]
}

const GraphPage = () => {
    const [resData, setResData] = useState()

    const getData = async () => {
        const { data } = await getGraphData()
        console.log(data)
        setResData(data)
    }

    useEffect(() => {
        getData()
    }, [])
    return (
        <div>
            <KnowledgeGraph resData={resData} />
        </div>
    )
}

export default GraphPage