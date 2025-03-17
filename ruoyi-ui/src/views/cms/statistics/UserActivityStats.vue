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
                    <div class="stat-card">
                        <div class="card-title">总访问量</div>
                        <div class="card-body">
                            <span class="card-value">{{ totalVisits }}</span>
                            <i class="el-icon-view card-icon"></i>
                        </div>
                    </div>
                </el-col>
                <el-col :span="8">
                    <div class="stat-card">
                        <div class="card-title">活跃用户</div>
                        <div class="card-body">
                            <span class="card-value">{{ activeUsers }}</span>
                            <i class="el-icon-user card-icon"></i>
                        </div>
                    </div>
                </el-col>
                <el-col :span="8">
                    <div class="stat-card">
                        <div class="card-title">平均停留时间</div>
                        <div class="card-body">
                            <span class="card-value">{{ avgTimeSpent }}s</span>
                            <i class="el-icon-time card-icon"></i>
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
            request.get('/cms/userActivity/popular-keywords').then(response => {
                console.log('关键词数据:', response.data)
                this.keywordsData = response.data || []

                // 计算总访问量
                this.calculateTotals()
            }).catch(error => {
                console.error('获取关键词统计失败', error)
            })

            // 获取停留时间统计
            request.get('/cms/userActivity/average-time').then(response => {
                console.log('停留时间数据:', response.data)
                this.timeSpentData = response.data || []
                this.updateTimeSpentChart()

                // 计算平均停留时间
                if (this.timeSpentData.length > 0) {
                    const sum = this.timeSpentData.reduce((acc, item) => acc + item.timeSpent, 0)
                    this.avgTimeSpent = Math.round(sum / this.timeSpentData.length)
                }
            }).catch(error => {
                console.error('获取停留时间统计失败', error)
            })
        },
        calculateTotals() {
            // 计算总访问量和活跃用户数
            this.totalVisits = this.pageViewsData.reduce((sum, item) => sum + item.visits, 0)
            // 假设活跃用户数是页面访问数的70%
            this.activeUsers = Math.round(this.totalVisits * 0.7)
        },
        fetchPageViewsData() {
            request.get('/cms/userActivity/statistics').then(response => {
                console.log('获取到的页面访问数据:', response.data)
                if (response.code === 200) {
                    const data = response.data
                    if (data && data.pageViews && data.pageViews.length > 0) {
                        this.pageViewsData = data.pageViews
                        this.updatePageViewsChart()
                        this.calculateTotals()
                    } else {
                        console.warn('页面访问数据为空')
                        this.$message.warning('没有页面访问数据')
                    }
                } else {
                    console.error('请求页面访问数据失败:', response.msg)
                }
                this.loading = false
            }).catch(error => {
                console.error('获取页面访问统计失败', error)
                this.loading = false
            })
        },
        fetchContentTypeData() {
            request.get('/cms/userActivity/popular-content-types').then(response => {
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
            }).catch(error => {
                console.error('获取内容类型统计失败', error)
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

            const option = {
                title: {
                    text: '页面访问统计',
                    left: 'center',
                    textStyle: {
                        color: '#303133',
                        fontWeight: 'normal'
                    }
                },
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
                    data: this.pageViewsData.map(item => item.page || ''),
                    axisLabel: {
                        interval: 0,
                        rotate: 30
                    }
                },
                yAxis: {
                    type: 'value'
                },
                series: [{
                    data: this.pageViewsData.map(item => {
                        return {
                            value: item.visits || 0,
                            itemStyle: {
                                color: this.getGradientColor(item.visits)
                            }
                        }
                    }),
                    type: 'bar',
                    showBackground: true,
                    backgroundStyle: {
                        color: 'rgba(220, 220, 220, 0.2)'
                    }
                }]
            }

            this.pageViewsChart.setOption(option)
        },
        updateTimeSpentChart() {
            if (!this.timeSpentChart || !this.timeSpentData) return

            const option = {
                title: {
                    text: '用户停留时间',
                    left: 'center',
                    textStyle: {
                        color: '#303133',
                        fontWeight: 'normal'
                    }
                },
                tooltip: {
                    trigger: 'axis'
                },
                grid: {
                    left: '3%',
                    right: '4%',
                    bottom: '3%',
                    containLabel: true
                },
                xAxis: {
                    type: 'category',
                    boundaryGap: false,
                    data: this.timeSpentData.map(item => item.page),
                    axisLabel: {
                        interval: 0,
                        rotate: 30
                    }
                },
                yAxis: {
                    type: 'value',
                    axisLabel: {
                        formatter: '{value} 秒'
                    }
                },
                series: [{
                    name: '停留时间',
                    data: this.timeSpentData.map(item => item.timeSpent),
                    type: 'line',
                    smooth: true,
                    lineStyle: {
                        color: '#409EFF',
                        width: 3
                    },
                    areaStyle: {
                        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                            {
                                offset: 0,
                                color: 'rgba(64, 158, 255, 0.7)'
                            },
                            {
                                offset: 1,
                                color: 'rgba(64, 158, 255, 0.1)'
                            }
                        ])
                    },
                    symbol: 'circle',
                    symbolSize: 8
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
    }
}
</script>

<style scoped>
.dashboard-container {
    padding: 20px;
    background-color: #f5f7fa;
}

.stat-card {
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

.stat-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.card-title {
    font-size: 16px;
    color: #909399;
    margin-bottom: 10px;
}

.card-body {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.card-value {
    font-size: 30px;
    font-weight: bold;
    color: #303133;
}

.card-icon {
    font-size: 48px;
    color: rgba(64, 158, 255, 0.2);
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