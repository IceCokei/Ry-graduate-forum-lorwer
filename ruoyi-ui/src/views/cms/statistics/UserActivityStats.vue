<template>
    <div class="app-container">
        <el-breadcrumb separator="/">
            <el-breadcrumb-item>首页</el-breadcrumb-item>
            <el-breadcrumb-item>用户统计</el-breadcrumb-item>
        </el-breadcrumb>
        <div class="dashboard-container">
            <!-- 顶部卡片 -->
            <el-row :gutter="20">
                <el-col :span="8">
                    <div class="card-panel">
                        <div class="card-panel-icon">
                            <svg-icon icon-class="eye" class-name="card-panel-icon-eye" />
                        </div>
                        <div class="card-panel-description">
                            <div class="card-panel-text">总访问量</div>
                            <div class="card-panel-num">{{ totalVisits }}</div>
                        </div>
                    </div>
                </el-col>
                <el-col :span="8">
                    <div class="card-panel">
                        <div class="card-panel-icon">
                            <svg-icon icon-class="peoples" class-name="card-panel-icon-people" />
                        </div>
                        <div class="card-panel-description">
                            <div class="card-panel-text">活跃用户</div>
                            <div class="card-panel-num">{{ activeUsers }}</div>
                        </div>
                    </div>
                </el-col>
                <el-col :span="8">
                    <div class="card-panel">
                        <div class="card-panel-icon">
                            <svg-icon icon-class="clock" class-name="card-panel-icon-time" />
                        </div>
                        <div class="card-panel-description">
                            <div class="card-panel-text">平均停留时间</div>
                            <div class="card-panel-num">{{ formattedAvgTime }}</div>
                        </div>
                    </div>
                </el-col>
            </el-row>

            <!-- 图表区域 -->
            <el-row :gutter="20" class="chart-row">
                <el-col :span="12">
                    <el-card class="chart-card" shadow="hover">
                        <div slot="header" class="clearfix">
                            <span>页面访问统计</span>
                        </div>
                        <div id="pageViewsChart" style="width: 100%; height: 300px"></div>
                    </el-card>
                </el-col>
                <el-col :span="12">
                    <el-card class="chart-card" shadow="hover">
                        <div slot="header" class="clearfix">
                            <span>用户停留时间</span>
                        </div>
                        <div id="timeSpentChart" style="width: 100%; height: 300px"></div>
                    </el-card>
                </el-col>
            </el-row>

            <!-- 热门关键词和内容类型 -->
            <el-row :gutter="20" class="chart-row">
                <el-col :span="12">
                    <el-card class="chart-card" shadow="hover">
                        <div slot="header" class="clearfix">
                            <span>热门搜索关键词</span>
                        </div>
                        <el-table :data="keywordsData" style="width: 100%"
                            :header-cell-style="{ background: '#f5f7fa', color: '#606266' }" border>
                            <el-table-column prop="keyword" label="关键词"></el-table-column>
                            <el-table-column prop="count" label="搜索次数"></el-table-column>
                            <el-table-column prop="percentage" label="占比">
                                <template slot-scope="scope">
                                    <el-progress :percentage="scope.row.percentage"
                                        :color="progressColor(scope.row.percentage)">
                                    </el-progress>
                                </template>
                            </el-table-column>
                        </el-table>
                    </el-card>
                </el-col>
                <el-col :span="12">
                    <el-card class="chart-card" shadow="hover">
                        <div slot="header" class="clearfix">
                            <span>内容类型访问统计</span>
                        </div>
                        <div id="contentTypeChart" style="width: 100%; height: 300px"></div>
                    </el-card>
                </el-col>
            </el-row>
        </div>
    </div>
</template>

<script>
import * as echarts from 'echarts'
import request from '@/utils/request'
import { getUserActivityStatistics, getTimeSpentStatistics, getPopularKeywords, getContentTypeStatistics } from '@/api/cms/userActivity'

export default {
    name: "UserActivityStats",
    data() {
        return {
            pageViewsChart: null,
            timeSpentChart: null,
            contentTypeChart: null,
            pageViewsData: [],
            timeSpentData: [],
            contentTypeData: [],
            keywordsData: [],
            loading: true,
            // 新增数据
            totalVisits: 0,
            activeUsers: 0,
            avgTimeSpent: 0
        }
    },
    created() {
        this.fetchData()
    },
    mounted() {
        this.fetchPageViewsData()
        this.fetchTimeSpentData()
        this.fetchPopularKeywords()
        this.fetchContentTypeData()
        this.$nextTick(() => {
            this.initCharts()
        })
        window.addEventListener('resize', this.resizeCharts)
    },
    beforeDestroy() {
        window.removeEventListener('resize', this.resizeCharts)
    },
    methods: {
        fetchData() {
            this.loading = true

            // 获取页面访问统计
            this.fetchPageViewsData()

            // 获取内容类型统计
            this.fetchContentTypeData()

            // 获取关键词统计
            this.fetchPopularKeywords()

            // 获取停留时间统计
            this.fetchTimeSpentData()
        },
        calculateTotals() {
            if (!this.pageViewsData) return

            // 计算总访问量
            this.totalVisits = this.pageViewsData.reduce((sum, item) => sum + (item.count || 0), 0)

            // 计算活跃用户数
            const userSet = new Set()
            this.pageViewsData.forEach(item => {
                if (item.users) {
                    item.users.forEach(user => userSet.add(user))
                }
            })
            this.activeUsers = userSet.size

            // 计算平均停留时间，确保所有时间都被计算，不论多长
            if (this.timeSpentData && this.timeSpentData.length > 0) {
                const sum = this.timeSpentData.reduce((acc, item) => acc + item.timeSpent, 0)
                const totalRecords = this.timeSpentData.reduce((sum, item) => sum + (item.count || 1), 0)
                this.avgTimeSpent = totalRecords > 0 ? Math.floor(sum / totalRecords) : 0
            }
        },
        fetchPageViewsData() {
            this.loading = true
            getUserActivityStatistics().then(response => {
                if (response.code === 200) {
                    // 记录完整响应
                    console.log('用户活动统计完整响应:', response.data)

                    // 更新数据
                    this.totalVisits = response.data.totalVisits || 0
                    this.avgTimeSpent = response.data.avgTimeSpent || 0
                    this.activeUsers = response.data.activeUsers || 0

                    // 特别注意页面访问数据格式
                    // 后端API实际返回是pageViewsData，如果不存在，还可能是pageViews
                    this.pageViewsData = response.data.pageViewsData || response.data.pageViews || []

                    console.log('pageViewsData处理前:', this.pageViewsData)

                    // 数据格式适配：确保结构一致
                    if (this.pageViewsData && this.pageViewsData.length > 0) {
                        // 如果返回数据没有page和visits属性，尝试适配其他可能的属性名
                        if (this.pageViewsData[0].page === undefined && this.pageViewsData[0].path !== undefined) {
                            this.pageViewsData = this.pageViewsData.map(item => ({
                                page: item.path,
                                visits: item.count || item.visits || 0
                            }))
                        }

                        console.log('pageViewsData处理后:', this.pageViewsData)
                        this.updatePageViewsChart()
                    } else {
                        console.warn('页面访问数据为空')
                    }
                } else {
                    console.error('请求用户活动统计失败:', response.msg)
                }
                this.loading = false
            }).catch(error => {
                console.error('获取用户活动统计失败', error)
                this.loading = false
            })
        },
        fetchContentTypeData() {
            this.typesLoading = true
            getContentTypeStatistics().then(response => {
                console.log('获取到的内容类型数据:', response.data)
                if (response.code === 200) {
                    const data = response.data
                    if (data && data.length > 0) {
                        this.contentTypeData = data
                        this.updateContentTypeChart()
                    } else {
                        console.warn('内容类型数据为空')
                    }
                } else {
                    console.error('请求内容类型统计失败:', response.msg)
                }
                this.typesLoading = false
            }).catch(error => {
                console.error('获取内容类型统计失败', error)
                this.typesLoading = false
            })
        },
        fetchTimeSpentData() {
            this.timeLoading = true
            getTimeSpentStatistics().then(response => {
                if (response.code === 200) {
                    console.log('获取到的停留时间数据:', response.data)
                    this.timeSpentData = response.data || []

                    // 确保时间数据非空后更新图表
                    if (this.timeSpentData && this.timeSpentData.length > 0) {
                        this.updateTimeSpentChart()
                    } else {
                        console.warn('停留时间数据为空')
                    }
                }
                this.timeLoading = false
            }).catch(error => {
                console.error('获取停留时间统计数据失败:', error)
                this.timeLoading = false
            })
        },
        fetchPopularKeywords() {
            this.keywordsLoading = true
            getPopularKeywords().then(response => {
                if (response.code === 200) {
                    this.keywordsData = response.data || []
                }
                this.keywordsLoading = false
            }).catch(() => {
                this.keywordsLoading = false
            })
        },
        initCharts() {
            // 初始化页面访问图表
            this.pageViewsChart = echarts.init(document.getElementById('pageViewsChart'))
            this.updatePageViewsChart()

            // 初始化停留时间图表
            this.timeSpentChart = echarts.init(document.getElementById('timeSpentChart'))
            this.updateTimeSpentChart()

            // 初始化内容类型图表
            this.contentTypeChart = echarts.init(document.getElementById('contentTypeChart'))
            this.updateContentTypeChart()
        },
        updatePageViewsChart() {
            if (!this.pageViewsChart || !this.pageViewsData) return

            // 记录原始数据用于调试
            console.log('页面访问数据(原始):', this.pageViewsData)

            // 数据适配：确保使用正确的属性名
            const chartData = this.pageViewsData.map(item => {
                return {
                    name: item.page || '',
                    value: item.visits || 0
                }
            })

            console.log('转换后的图表数据:', chartData)

            // 提取X轴数据和系列数据
            const xAxisData = chartData.map(item => item.name)
            const seriesData = chartData.map(item => item.value)

            const option = {
                tooltip: {
                    trigger: 'axis',
                    axisPointer: {
                        type: 'shadow'
                    }
                },
                grid: {
                    left: '3%',
                    right: '4%',
                    bottom: '3%',
                    containLabel: true
                },
                xAxis: {
                    type: 'category',
                    data: xAxisData,
                    axisLabel: {
                        interval: 0,
                        rotate: 30,
                        textStyle: {
                            fontSize: 12
                        }
                    }
                },
                yAxis: {
                    type: 'value'
                },
                series: [{
                    name: '访问次数',
                    type: 'bar',
                    data: seriesData,
                    itemStyle: {
                        color: '#409EFF'
                    }
                }]
            }

            this.pageViewsChart.setOption(option)
        },
        updateTimeSpentChart() {
            if (!this.timeSpentChart || !this.timeSpentData) return

            // 记录原始数据用于调试
            console.log('停留时间数据(原始):', this.timeSpentData)

            // 数据适配：确保使用正确的属性名
            const chartData = this.timeSpentData.map(item => {
                return {
                    name: item.path || '',  // 后端返回的是path而不是page
                    value: item.timeSpent || 0
                }
            })

            console.log('转换后的时间图表数据:', chartData)

            // 提取X轴数据和系列数据
            const xAxisData = chartData.map(item => item.name)
            const seriesData = chartData.map(item => item.value)

            const option = {
                tooltip: {
                    trigger: 'axis'
                },
                xAxis: {
                    type: 'category',
                    data: xAxisData,
                    axisLabel: {
                        interval: 0,
                        rotate: 30
                    }
                },
                yAxis: {
                    type: 'value',
                    name: '停留时间(秒)'
                },
                series: [{
                    name: '停留时间',
                    type: 'line',
                    smooth: true,
                    data: seriesData,
                    areaStyle: {
                        opacity: 0.3,
                        color: {
                            type: 'linear',
                            x: 0,
                            y: 0,
                            x2: 0,
                            y2: 1,
                            colorStops: [{
                                offset: 0, color: '#67C23A'
                            }, {
                                offset: 1, color: 'rgba(103, 194, 58, 0.1)'
                            }]
                        }
                    },
                    lineStyle: {
                        width: 3,
                        color: '#67C23A'
                    },
                    itemStyle: {
                        color: '#67C23A'
                    }
                }]
            }

            this.timeSpentChart.setOption(option)
        },
        updateContentTypeChart() {
            if (!this.contentTypeChart || !this.contentTypeData) return

            const option = {
                title: {
                    text: '内容类型访问统计',
                    left: 'center',
                    textStyle: {
                        color: '#303133',
                        fontWeight: 'normal'
                    }
                },
                tooltip: {
                    trigger: 'item',
                    formatter: '{a} <br/>{b} : {c} ({d}%)'
                },
                legend: {
                    orient: 'vertical',
                    left: 'left',
                    data: this.contentTypeData.map(item => item.type || '')
                },
                series: [
                    {
                        name: '访问次数',
                        type: 'pie',
                        radius: '55%',
                        center: ['50%', '60%'],
                        data: this.contentTypeData.map((item, index) => ({
                            name: item.type || '',
                            value: item.count || 0,
                            itemStyle: {
                                color: this.getPieColor(index)
                            }
                        })),
                        emphasis: {
                            itemStyle: {
                                shadowBlur: 10,
                                shadowOffsetX: 0,
                                shadowColor: 'rgba(0, 0, 0, 0.5)'
                            }
                        },
                        label: {
                            show: true,
                            formatter: '{b}: {d}%'
                        },
                        labelLine: {
                            show: true
                        }
                    }
                ]
            }

            this.contentTypeChart.setOption(option)
        },
        resizeCharts() {
            if (this.pageViewsChart) this.pageViewsChart.resize()
            if (this.timeSpentChart) this.timeSpentChart.resize()
            if (this.contentTypeChart) this.contentTypeChart.resize()
        },
        initPageViewsChart(data) {
            if (data) {
                this.pageViewsData = data
            }
            this.updatePageViewsChart()
        },
        initTimeSpentChart(data) {
            if (data) {
                this.timeSpentData = data
            }
            this.updateTimeSpentChart()
        },
        initContentTypeChart(data) {
            if (data) {
                this.contentTypeData = data
            }
            this.updateContentTypeChart()
        },
        // 自定义颜色函数
        getGradientColor(value) {
            // 根据数值返回不同深度的蓝色
            const intensity = Math.min(100, value) / 100;
            return new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: `rgba(83, 142, 213, ${0.8 + intensity * 0.2})` },
                { offset: 1, color: `rgba(83, 142, 213, ${0.3 + intensity * 0.4})` }
            ]);
        },
        getPieColor(index) {
            // 现代化配色方案
            const colors = [
                '#5B8FF9', '#5AD8A6', '#5D7092', '#F6BD16',
                '#6F5EF9', '#6DC8EC', '#945FB9', '#FF9845',
                '#1E9493', '#FF99C3', '#AFD5FF', '#C2B4D2'
            ];
            return colors[index % colors.length];
        },
        progressColor(percentage) {
            if (percentage < 30) {
                return '#909399';
            } else if (percentage < 70) {
                return '#409EFF';
            } else {
                return '#67C23A';
            }
        }
    },
    computed: {
        formattedAvgTime() {
            // 将秒转为分秒格式显示
            const seconds = this.avgTimeSpent || 0;
            const minutes = Math.floor(seconds / 60);
            const remainingSeconds = seconds % 60;

            if (minutes > 0) {
                return `${minutes}m ${remainingSeconds}s`;
            }
            return `${seconds}s`;
        }
    }
}
</script>

<style scoped>
.dashboard-container {
    padding: 20px;
    background-color: #f5f7fa;
}

.card-panel {
    background-color: #fff;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
    height: 120px;
    margin-bottom: 20px;
    position: relative;
    overflow: hidden;
    transition: all 0.3s;
}

.card-panel:hover {
    transform: translateY(-5px);
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.card-panel-icon {
    float: left;
    margin-right: 15px;
}

.card-panel-description {
    overflow: hidden;
}

.card-panel-text {
    font-size: 16px;
    color: #909399;
    margin-bottom: 10px;
}

.card-panel-num {
    font-size: 30px;
    font-weight: bold;
    color: #303133;
}

.chart-row {
    margin-bottom: 20px;
}

.chart-card {
    margin-bottom: 20px;
    border-radius: 8px;
    border: none;
}

.chart-card .el-card__header {
    padding: 15px 20px;
    border-bottom: 1px solid #ebeef5;
    font-weight: 500;
    color: #303133;
}

.chart-card .el-card__body {
    padding: 20px;
}

/* 表格样式优化 */
.el-table {
    border-radius: 4px;
    overflow: hidden;
}

.el-table th {
    background-color: #f5f7fa !important;
}

.el-progress-bar__inner {
    transition: all 0.3s;
}

/* 响应式调整 */
@media (max-width: 768px) {
    .el-col {
        width: 100%;
    }
}
</style>