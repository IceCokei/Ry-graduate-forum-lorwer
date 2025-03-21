import Vue from 'vue'

import Cookies from 'js-cookie'

import Element from 'element-ui'
import './assets/styles/element-variables.scss'

import '@/assets/styles/index.scss' // global css
import '@/assets/styles/ruoyi.scss' // ruoyi css
import App from './App'
import store from './store'
import router from './router'
import directive from './directive' // directive
import plugins from './plugins' // plugins
import { download } from '@/utils/request'
// 导入request
import request from '@/utils/request'

import './assets/icons' // icon
import './permission' // permission control
import { getDicts } from "@/api/system/dict/data";
import { getConfigKey } from "@/api/system/config";
import { parseTime, resetForm, addDateRange, selectDictLabel, selectDictLabels, handleTree } from "@/utils/ruoyi";
// 分页组件
import Pagination from "@/components/Pagination";
// 自定义表格工具组件
import RightToolbar from "@/components/RightToolbar"
// 富文本组件
import Editor from "@/components/Editor"
import cmsEditor from "@/components/cmsEditor"
// 文件上传组件
import FileUpload from "@/components/FileUpload"
// 图片上传组件
import ImageUpload from "@/components/ImageUpload"
// 字典标签组件
import DictTag from '@/components/DictTag'
// 头部标签组件
import VueMeta from 'vue-meta'
// 字典数据组件
import DictData from '@/components/DictData'

// 全局方法挂载
Vue.prototype.getDicts = getDicts
Vue.prototype.getConfigKey = getConfigKey
Vue.prototype.parseTime = parseTime
Vue.prototype.resetForm = resetForm
Vue.prototype.addDateRange = addDateRange
Vue.prototype.selectDictLabel = selectDictLabel
Vue.prototype.selectDictLabels = selectDictLabels
Vue.prototype.download = download
Vue.prototype.handleTree = handleTree

// 全局组件挂载
Vue.component('DictTag', DictTag)
Vue.component('Pagination', Pagination)
Vue.component('RightToolbar', RightToolbar)
Vue.component('Editor', Editor)
Vue.component('cmsEditor', cmsEditor)
Vue.component('FileUpload', FileUpload)
Vue.component('ImageUpload', ImageUpload)

Vue.use(directive)
Vue.use(plugins)
Vue.use(VueMeta)
DictData.install()

/**
 * If you don't want to use mock-server
 * you want to use MockJs for mock api
 * you can execute: mockXHR()
 *
 * Currently MockJs will be used in the production environment,
 * please remove it before going online! ! !
 */

Vue.use(Element, {
  size: Cookies.get('size') || 'medium' // set element-ui default size
})

Vue.config.productionTip = false

// 全局记录用户访问开始时间
let visitStartTime = new Date().getTime();
let currentPath = '';

// 添加页面关闭事件监听
window.addEventListener('beforeunload', function () {
  // 只有当有有效的路径时才记录
  if (currentPath) {
    const now = new Date().getTime();
    const timeSpent = Math.floor((now - visitStartTime) / 1000);

    // 获取内容类型
    let contentType = '其他';
    if (currentPath.includes('/blog/')) {
      contentType = '文章';
    } else if (currentPath.includes('/forum/')) {
      contentType = '论坛帖子';
    } else if (currentPath.includes('/cms/statistics/')) {
      contentType = '用户统计';
    } else if (currentPath.includes('/system/')) {
      contentType = '系统管理';
    } else if (currentPath.includes('/monitor/')) {
      contentType = '系统监控';
    }

    // 构建数据
    const activityData = {
      path: currentPath,
      timeSpent: timeSpent,
      contentType: contentType,
      device: navigator.userAgent,
      browser: getBrowserInfo(),
      timestamp: new Date()
    };

    // 使用同步请求确保数据发送
    const xhr = new XMLHttpRequest();
    xhr.open('POST', '/cms/userActivity/track', false); // 同步请求
    xhr.setRequestHeader('Content-Type', 'application/json;charset=UTF-8');
    xhr.setRequestHeader('Authorization', 'Bearer ' + Cookies.get('Admin-Token')); // 添加认证
    xhr.send(JSON.stringify(activityData));
  }
});

// 修改现有的路由守卫代码
router.afterEach((to, from) => {
  // 排除一些不需要记录的页面，如登录、错误页面等
  const excludePaths = ['/login', '/auth-redirect', '/404', '/401'];
  if (!excludePaths.includes(to.path)) {
    // 更新当前路径
    currentPath = to.path;

    // 创建记录用户行为的函数
    const recordUserActivity = () => {
      // 获取停留时间（不设置最大值限制）
      const now = new Date().getTime();
      const timeSpent = from.name ? Math.floor((now - visitStartTime) / 1000) : 0;

      // 获取内容类型
      let contentType = '其他';
      if (to.path.includes('/blog/')) {
        contentType = '文章';
      } else if (to.path.includes('/forum/')) {
        contentType = '论坛帖子';
      } else if (to.path.includes('/cms/statistics/')) {
        contentType = '用户统计';
      } else if (to.path.includes('/system/')) {
        contentType = '系统管理';
      } else if (to.path.includes('/monitor/')) {
        contentType = '系统监控';
      }

      // 构建数据
      const activityData = {
        path: to.path,
        timeSpent: timeSpent,
        contentType: contentType,
        device: navigator.userAgent,
        browser: getBrowserInfo(),
        timestamp: new Date()
      };

      // 发送请求
      request({
        url: '/cms/userActivity/track',
        method: 'post',
        data: activityData
      }).then(() => {
        console.log('用户行为数据已记录');
      }).catch(error => {
        console.error('记录用户行为失败', error);
      });
    };

    // 重置访问开始时间为当前时间
    visitStartTime = new Date().getTime();

    // 如果不是首次访问，记录上一页面的访问情况
    if (from.name) {
      recordUserActivity();
    }
  }
});

// 辅助函数：获取浏览器信息
function getBrowserInfo() {
  const ua = navigator.userAgent;
  let browser = 'Unknown';

  if (ua.indexOf('Edge') > -1) {
    browser = 'Edge';
  } else if (ua.indexOf('Chrome') > -1) {
    browser = 'Chrome';
  } else if (ua.indexOf('Firefox') > -1) {
    browser = 'Firefox';
  } else if (ua.indexOf('Safari') > -1) {
    browser = 'Safari';
  } else if (ua.indexOf('MSIE') > -1 || ua.indexOf('Trident') > -1) {
    browser = 'IE';
  }

  return browser;
}

new Vue({
  el: '#app',
  router,
  store,
  render: h => h(App)
})
