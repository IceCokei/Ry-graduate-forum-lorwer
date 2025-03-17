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

// 添加全局导航守卫，记录用户行为
router.afterEach((to, from) => {
  // 排除一些不需要记录的页面，如登录、错误页面等
  const excludePaths = ['/login', '/auth-redirect', '/404', '/401'];
  if (!excludePaths.includes(to.path)) {
    // 创建记录用户行为的函数
    const recordUserActivity = () => {
      // 获取停留时间
      const now = new Date().getTime();
      const timeSpent = from.name ? Math.floor((now - router.app.$options.visitStartTime) / 1000) : 0;

      // 获取内容类型
      let contentType = '其他';
      if (to.path.includes('/cms/post')) {
        contentType = '文章';
      } else if (to.path.includes('/cms/tag')) {
        contentType = '标签';
      } else if (to.path.includes('/cms/index')) {
        contentType = '首页';
      }

      // 构建数据
      const activityData = {
        path: to.path,
        timeSpent: timeSpent > 0 ? timeSpent : 0,
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

    // 记录当前时间作为新页面访问的开始时间
    router.app.$options.visitStartTime = new Date().getTime();

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
