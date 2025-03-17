<template>

  <el-container>
    <div class="background" :style="backgroundImage"></div>
    <cmsNavBar></cmsNavBar>
    <keep-alive>
      <cmsMain></cmsMain>
    </keep-alive>
    <el-footer height="40px">
      <cmsFooter></cmsFooter>
    </el-footer>
  </el-container>
</template>

<script>
import cmsMain from './main.vue'
import cmsNavBar from './NavBar.vue'
import cmsFooter from './Footer.vue'
// 引入背景图
import backgroundImages from './backgroundImages.js'

export default {
  data() {
    return {
      backgroundImages,
      startTime: new Date(), // 记录页面加载时间
      currentPath: '',
      userActivities: {
        pageViews: {},
        searchKeywords: [],
        totalTimeSpent: 0
      }
    };
  },
  methods: {
    handleSelect(key, keyPath) {
      console.log(key, keyPath);
    },

    // 跟踪页面访问
    trackPageView(page) {
      if (!this.userActivities.pageViews[page]) {
        this.userActivities.pageViews[page] = 0;
      }
      this.userActivities.pageViews[page]++;

      // 可以通过API将数据发送到后端
      // this.sendActivityDataToServer();
    },

    // 跟踪搜索关键词
    trackSearchKeyword(keyword) {
      this.userActivities.searchKeywords.push({
        keyword,
        timestamp: new Date()
      });

      // 可以通过API将数据发送到后端
      // this.sendActivityDataToServer();
    },

    // 计算页面停留时间
    calculateTimeSpent() {
      const now = new Date();
      const timeSpent = (now - this.startTime) / 1000; // 转换为秒
      this.userActivities.totalTimeSpent += timeSpent;
      this.startTime = now; // 重置计时器

      // 可以通过API将数据发送到后端
      // this.sendActivityDataToServer();
    },

    // 修复发送数据方法
    sendActivityDataToServer() {
      // 构建完整的有效载荷
      const payload = {
        userId: this.$store.state.user.userId || -1, // 使用实际用户ID，匿名用户用-1
        username: this.$store.state.user.name || 'anonymous',
        path: this.currentPath,
        timeSpent: Math.round(this.userActivities.totalTimeSpent), // 确保是整数
        contentType: this.getContentTypeFromPath(),
        device: navigator.userAgent,
        browser: this.getBrowserInfo(),
        timestamp: new Date()
      };

      console.log('发送用户活动数据:', JSON.stringify(payload));

      // 使用正确的API路径并添加返回处理
      this.$http.post('/cms/userActivity/track', payload).then(response => {
        console.log('活动数据发送成功:', response.data);
      }).catch(error => {
        console.error('发送活动数据失败:', error);
      });
    },

    // 辅助方法获取浏览器信息
    getBrowserInfo() {
      const ua = navigator.userAgent;
      let browser = "Unknown";

      if (ua.indexOf("Firefox") > -1) {
        browser = "Firefox";
      } else if (ua.indexOf("Chrome") > -1) {
        browser = "Chrome";
      } else if (ua.indexOf("Safari") > -1) {
        browser = "Safari";
      } else if (ua.indexOf("MSIE") > -1 || ua.indexOf("Trident") > -1) {
        browser = "IE";
      } else if (ua.indexOf("Edge") > -1) {
        browser = "Edge";
      }

      return browser;
    },

    // 从当前路径获取内容类型
    getContentTypeFromPath() {
      const path = this.currentPath;
      if (path.includes('/post/')) {
        return '文章详情';
      } else if (path.includes('/type/')) {
        return '分类浏览';
      } else if (path.includes('/tag/')) {
        return '标签浏览';
      } else if (path.includes('/search')) {
        return '搜索结果';
      }
      return '首页';
    }
  },
  components: {
    cmsMain,
    cmsNavBar,
    cmsFooter
  },
  computed: {
    backgroundImage() {
      // 根据背景图数组的长度随机选择索引
      const randIndex = Math.floor(Math.random() * this.backgroundImages.length)
      return {
        // 获取对应的图片资源并将其设置到`background-image`属性上
        backgroundImage: `url(${this.backgroundImages[randIndex]})`
      }
    }
  },
  created() {
    // 马上记录初始页面访问
    this.currentPath = this.$route.path;
    this.trackPageView(this.currentPath);

    // 设置计时器，每30秒发送一次数据（比1分钟更频繁确保数据被记录）
    this.timeTracker = setInterval(() => {
      this.calculateTimeSpent();
      this.sendActivityDataToServer(); // 确保定期发送数据
    }, 30000); // 30秒

    console.log('用户行为追踪已启动'); // 调试日志
  },
  beforeDestroy() {
    // 清除定时器
    clearInterval(this.timeTracker);

    // 在页面离开前计算最终的停留时间并发送数据
    this.calculateTimeSpent();
    this.sendActivityDataToServer();
  },
  watch: {
    // 监听路由变化，记录页面切换
    '$route'(to, from) {
      // 记录离开页面的停留时间
      this.calculateTimeSpent();

      // 记录新页面访问
      this.currentPath = to.path;
      this.trackPageView(this.currentPath);
    }
  }
}
</script>

<style scoped>
.el-footer {
  background-color: rgba(84, 92, 100, 0.5);
}

.background {
  background-repeat: no-repeat;
  background-size: cover;
  margin: 0px;
  padding: 0px;
  top: 0;
  width: 100%;
  height: 120vh;
  position: fixed;
  z-index: -1;
}

@media screen and (max-width: 768px) {
  .title {
    width: 100%;
    background-position-x: center;
    background-position-y: 0;
  }
}
</style>
