# smart-education
<div align="center">
  <img src="https://img.shields.io/badge/Project-Smart_Lesson_Preparation-blue" alt="Project Name">
  <h1>📚 小智备课 (SmartPrep)</h1>
  <h3>基于多模态大模型的人工智能通识课教师智能备课系统（智能体+AI大模型+虚拟人技术）</h3>

  <p>
    <a href="#-项目背景-background">项目背景</a> •
    <a href="#-核心功能-features">核心功能</a> •
    <a href="#-技术架构-architecture">技术架构</a> •
    <a href="#-系统演示-demo">系统演示</a> •
    <a href="#-快速开始-quick-start">快速开始</a>
  </p>
  
  <p>
    <img src="https://img.shields.io/badge/Backend-SpringBoot_3.0-green" alt="SpringBoot">
    <img src="https://img.shields.io/badge/AI_Service-Python_Flask-yellow" alt="Python">
    <img src="https://img.shields.io/badge/Frontend-React-blue" alt="React">
    <img src="https://img.shields.io/badge/LLM-DeepSeek_&_Kimi-purple" alt="LLM">
    <img src="https://img.shields.io/badge/Platform-Loongson_Arch-red" alt="Loongson">
  </p>
</div>

---

## 📖 项目背景 (Background)

在人工智能普及的今天，教师在备课过程中面临着**资源获取难、教案编写繁琐、个性化教学实施困难**等痛点。为了响应国家“人工智能+教育”的战略，我们开发了**“小智备课”**。

本项目不仅仅是一个简单的教案编辑器，它是一个集成了**Coze智能体、DeepSeek大模型、虚拟数字人影像技术**的综合性教学辅助平台。它运行在国产**龙芯平台**上，旨在通过 AI 技术实现：
1.  **备课提效**：从大纲到PPT再到微课视频的全流程自动化生成。
2.  **精准教学**：基于学情数据的 AI 分析与个性化资源推荐。
3.  **资源整合**：基于知识图谱的结构化教学资源管理。

---

## 🌟 核心亮点 (Highlights & Innovation)

### 1. 🤖 多模态数字人微课制作 (Digital Human Pipeline)
不同于传统的录屏，本系统实现了一套完整的 **PPT -> 视频** 自动化流水线。
*   **技术栈**：Flask + SadTalker + Wav2Lip + GPT-SoVITS。
*   **流程**：系统自动解析 PPT 批注生成台词 -> 克隆教师声音 (GPT-SoVITS) -> 驱动数字人面部表情与口型 (SadTalker/Wav2Lip) -> 视频融合。

### 2. 🧠 动态知识图谱构建 (Dynamic Knowledge Graph)
设计了“**系统模板层 + 学校定制层**”的双层图谱架构。
*   **算法**：实现了知识点复用机制与代理机制，支持学校在标准图谱基础上进行个性化定制，同时保证数据的唯一性和一致性。
*   **价值**：以知识点为核心，聚拢教案、习题、视频等所有资源，实现资源的高效检索。

### 3. 📝 智能备课 Agent (AI Agent Workflow)
利用 **Coze 平台** 编排工作流，集成了 **DeepSeek** 和 **Kimi** 的能力。
*   **功能**：支持长文本关键词提取（HanLP）、多角度写作提示（续写、结构优化）、以及基于 RAG（检索增强生成）的精准习题生成。

---

## 🛠️ 技术架构 (Technical Architecture)

本项目采用**前后端分离**架构，后端采用微服务思想，将核心业务与 AI 算力服务解耦。

### 💻 技术栈概览

| 模块 | 技术选型 | 说明 |
| :--- | :--- | :--- |
| **后端核心** | SpringBoot 3.0.5 + MyBatis-Plus | 核心业务逻辑，RESTful API |
| **AI 服务端** | Python (Flask) | 封装数字人生成、语音克隆等高算力任务 |
| **数据库/缓存** | MySQL 8.0 + Redis | 关系型数据存储 + 备课实时缓存/防丢失 |
| **即时通讯** | Netty + WebSocket | 实现 AI 对话流式传输与实时学情监控 |
| **前端** | React + Ant Design + ECharts | 响应式交互界面与数据可视化 |
| **NLP/大模型** | Coze API + DeepSeek + HanLP | 智能体编排与自然语言处理 |

### 🏗️ 系统架构图

![System Architecture](assert/系统架构图.png)

### 🔄 核心业务流程：数字人微课生成

![Digital Human Flow](assert/虚拟人授课视频制作流程.png)

---

## 📸 系统演示 (Screenshots)

### 1. 系统首页
![Home Page](assert/系统首页小智.png)

### 2. 智能备课板 & AI辅助工具
*支持 AI 续写、结构优化、素材生成的富文本编辑器。*
**AI一键生成丰富的教学大纲**
![Editor](assert/AI生成教学大纲.png)
**基于大纲一键创建教学设计**
![Editor](assert/基于大纲创建教学设计.png)
**教学设计编辑板**
![Editor](assert/备课板.png)

**辅助工具支持长文本关键词提取** 
**AI辅助工具1--文本AI续写功能**
![Editor](assert/Ai工具4.png)
**AI辅助工具2--AI图片生成**
![Editor](assert/AI工具2.png)
**AI辅助工具3--视频资源拉取**
![Editor](assert/AI工具3.png)

### 3. AI制作学习任务
**学习任务模块**
![homework make](assert/学习任务模块.png)
**AI制作学习任务+任务导出**
![homework make](assert/AI生成题目+导出.png)

### 4. 知识图谱管理
*可视化的知识点网络，支持点击节点获取关联资源。*
![Knowledge Graph](assert/知识图片1.png)
![Knowledge Graph](assert/知识图谱+资源管理1.png)
![Knowledge Graph](assert/知识图谱+资源管理2.png)
![Knowledge Graph](assert/知识图谱+资源管理3.png)

### 5. 数字人微课制作
*配置数字人形象、声音，一键合成教学视频。*
![Micro Class](assert/微课制作1.png)
![Micro Class](assert/微课制作2.png)
![Micro Class](assert/微课制作3.png)
![Micro Class](assert/微课制作4.png)
![Micro Class](assert/微课制作5.png)

### 🎥 数字人微课片段演示
点击下方视频直接播放：
[![Watch the video](https://i1.hdslb.com/bfs/archive/e665c0590c31782b6ba44d6292d436b30ccbc374.jpg@672w_378h_1c.avif )](https://www.bilibili.com/video/BV16UrzB7Ey1/?spm_id_from=333.1387.upload.video_card.click)

### 6. 学情分析与班级管控
*基于 ECharts 的多维度数据可视化分析。*
![Analytics](assert/班级管理面板.png)
![Analytics](assert/学情分析.png)
**学情分析包括题目回答情况分析、知识点掌握情况分析、针对性教学方案、推荐资源等**
![Analytics](assert/学情分析2.png)

---

## 🚀 快速开始 (How to Run)

### 环境要求
*   JDK 17+
*   Python 3.8+ (用于 AI 服务)
*   MySQL 8.0+
*   Redis 6.0+
*   Node.js 16+

### 后端启动 (Spring Boot)
1.  克隆仓库：`git clone [repo_url]`
2.  导入数据库脚本 `sql/tpa_system.sql`。
3.  修改 `application.yml` 中的数据库与 Redis 配置。
4.  运行 `TpaApplication.java`。

### AI 服务启动 (Python Flask)
1.  进入 `ai_server` 目录。
2.  安装依赖：`pip install -r requirements.txt` (需配置 PyTorch 环境)。
3.  下载预训练模型 (SadTalker, GPT-SoVITS) 至 `checkpoints` 目录。
4.  运行 `python app.py`。

### 前端启动
1.  进入 `frontend` 目录。
2.  安装依赖：`npm install`
3.  启动：`npm start`

---

## 📞 联系方式 (Contact)

*   **开发者**: [snowcat]
*   **Email**: [3356253976@qq.com]
*   **GitHub**: [https://github.com/liuchongliu35-ctrl]

---

