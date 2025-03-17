import request from '@/utils/request'

// 获取用户活动统计数据
export function getUserActivityStatistics() {
  return request({
    url: '/cms/userActivity/statistics',
    method: 'get'
  })
}

// 获取用户活动停留时间统计
export function getTimeSpentStatistics() {
  return request({
    url: '/cms/userActivity/timeSpent',
    method: 'get'
  })
}

// 获取热门搜索关键词
export function getPopularKeywords() {
  return request({
    url: '/cms/userActivity/keywords',
    method: 'get'
  })
}

// 获取内容类型统计
export function getContentTypeStatistics() {
  return request({
    url: '/cms/userActivity/contentTypes',
    method: 'get'
  })
} 