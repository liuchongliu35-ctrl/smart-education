import { http } from '@/utils/axios'




export function postRegisterUser(data) {
    return http({
        url: `/teacher/save`,
        method: 'POST',
        data: data
    })
}
export function getLastPreparation(tid) {
    return http({
        url: `/teachDesign/last/12`,
        method: 'GET',
    })
}

export function postUserLogin(value) {
    return http({
        url: `/teacher/login`,
        method: 'POST',
        data: value
    })
}

export function postAddschool(value) {
    return http({
        url: `/school`,
        method: 'POST',
        data: value
    })
}

export function getUserInfo() {
    return http({
        url: `/teacher/current`,
        method: 'GET',
    })
}

export function modifyUserInfo(value) {
    return http({
        url: `/teacher/update`,
        method: 'PUT',
        data: value
    })
}

//退出用户接口
export function postQuitUser() {
    return http({
        url: `/teacher/logout`,
        method: 'POST',
    })
}
