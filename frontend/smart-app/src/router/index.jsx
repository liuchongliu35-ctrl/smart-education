import { createBrowserRouter, redirect } from "react-router-dom"
import HomePage from "@/pages/Home/index"
import TestPage from "@/pages/Test/Test"
import EntryPage from "@/pages/Entry"
import PreparationBoard from "@/pages/preparationBoard"
import HomeWorkPage from "@/pages/homeWork"
import TextEditorPage from "@/pages/TextEditor"
import HomeContent from "@/pages/Home/homeContent"
import PreparationBoardHistory from '@/pages/preparationBoard/history'
import HomeworkEntry from "@/pages/homeWork/homeWorkEntry"
import ClassEntry from "@/pages/Class/classEntry"
import ClassPage from "@/pages/Class"
import BoardTemplate from "@/pages/preparationBoard/template"
import AIpreparation from "@/pages/preparationBoard/AIpreparation"
import ClassMemberPage from "@/pages/Class/classMember"
import ClassContent from "@/pages/Class/classContent"
import PreviewList from "@/pages/Class/previewList"
import HomeWorkList from "@/pages/Class/homeWorkList"
import ExamList from "@/pages/Class/examList"
import StudentAnswerDetail from "@/pages/Class/studentAnswerDetail"
import StudentTrackPage from "@/pages/homeWork/studenTrack"
import StudentHomeworkTrackPage from "../pages/homeWork/studentHomeworkTrack"
import PreviewWorkPage from "../pages/homeWork/previewWork"
import StudentHomeworkDetail from "../pages/Class/studentHomework"
import CustomTextEditor from "../component/Editor/CustomTextEditor"
import UserInfoPage from "../pages/Home/infoPage"
import NonePage from "@/pages/Home/404"
import GraphPage from "../pages/Graph/graphPage"
import VideoPage from "../pages/Video/videoPage"
import PPTpage from "../pages/PPT/PPTpage"

// const authLoader = async () => {
//   const token = localStorage.getItem('authToken');
//   if (!token) {
//     // 未登录时重定向到登录页（这里使用你的入口页作为登录页）
//     return redirect("/");
//   }
//   return null;
// };

const router = createBrowserRouter([
  {
    path: "/",
    element: <EntryPage />,
  },
  {
    path: "/404",
    element: <NonePage />,
  },
  {
    path: "/home",
    element: <HomePage />,
    // loader: authLoader,
    children: [
      { index: true, element: <HomeContent /> },
      { path: "preparation/:tsId/:schoolId", element: <PreparationBoard /> },
      { path: "preparationhistory", element: <PreparationBoardHistory /> },
      { path: "homeworkentry/:tsId/:schoolId", element: <HomeworkEntry /> },
      { path: "classentry", element: <ClassEntry /> },
      { path: "template/:tsId/:schoolId", element: <BoardTemplate /> },
      { path: "aipreparation", element: <AIpreparation /> },
      { path: "user", element: <UserInfoPage /> }

    ]
  },
  {
    path: "/texteditor/:design_id",
    element: <TextEditorPage />,
    // loader: authLoader,
  }
  , {
    path: "/customeditor",
    element: <CustomTextEditor />,
    // loader: authLoader,
  }
  , {
    path: "/homework/:h_id",
    element: <HomeWorkPage />,
    // loader: authLoader,
  }, {
    path: "/preview/:p_id",
    element: <PreviewWorkPage />,
    // loader: authLoader,
  },
  {
    path: "/test",
    element: <TestPage />,
    // loader: authLoader,
  }, {
    path: "/previewdetail/:u_id/:w_id",
    // loader: authLoader,
    element: <StudentTrackPage />
  }, {
    path: "/workdetail/:u_id/:w_id",
    element: <StudentHomeworkTrackPage />,
    // loader: authLoader,
  }, {
    path: "/class/:c_id",
    element: <ClassPage />,
    // loader: authLoader,
    children: [
      { index: true, element: <ClassContent /> },
      { path: "member", element: <ClassMemberPage /> },
      { path: "preview", element: <PreviewList /> },
      { path: "exercise", element: <HomeWorkList /> },
      { path: "exam", element: <ExamList /> },
      { path: "detail/:p_id", element: <StudentAnswerDetail /> },
      { path: "homeworkdetail/:h_id", element: <StudentHomeworkDetail /> }
    ]
  }, {
    path: "/graph/:tsId/:schoolId",
    element: <GraphPage />,
    // loader: authLoader,
  }, {
    path: "/video",
    element: <VideoPage />
  }, {
    path: "/ppt",
    element: <PPTpage />
  }
])

export default router