<template>
    <div class="app-container">
        <el-card class="box-card">
            <div slot="header" class="clearfix">
                <span>用户行为统计</span>
            </div>

            <!-- 页面访问统计 -->
            <el-row :gutter="20">
                <el-col :span="12">
                    <div id="pageViewsChart" style="width: 100%; height: 300px"></div>
                </el-col>
                <el-col :span="12">
                    <div id="timeSpentChart" style="width: 100%; height: 300px"></div>
                </el-col>
            </el-row>

            <el-divider></el-divider>

            <!-- 热门搜索关键词 -->
            <el-row>
                <el-col :span="24">
                    <h4>热门搜索关键词</h4>
                    <el-table :data="keywordsData" style="width: 100%">
                        <el-table-column prop="keyword" label="关键词"></el-table-column>
                        <el-table-column prop="count" label="搜索次数"></el-table-column>
                        <el-table-column prop="percentage" label="占比">
                            <template slot-scope="scope">
                                {{ scope.row.percentage }}%
                            </template>
                        </el-table-column>
                    </el-table>
                </el-col>
            </el-row>

            <el-divider></el-divider>

            <!-- 用户访问内容类型统计 -->
            <el-row>
                <el-col :span="24">
                    <div id="contentTypeChart" style="width: 100%; height: 300px"></div>
                </el-col>
            </el-row>
        </el-card>
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
            loading: true
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
            }).catch(error => {
                console.error('获取关键词统计失败', error)
            })

            // 获取停留时间统计
            request.get('/cms/userActivity/average-time').then(response => {
                console.log('停留时间数据:', response.data)
                this.timeSpentData = response.data || []
                this.updateTimeSpentChart()
            }).catch(error => {
                console.error('获取停留时间统计失败', error)
            })
        },
        fetchPageViewsData() {
            request.get('/cms/userActivity/statistics').then(response => {
                console.log('获取到的页面访问数据:', response.data)
                if (response.code === 200) {
                    const data = response.data
                    if (data && data.pageViews && data.pageViews.length > 0) {
                        this.pageViewsData = data.pageViews
                        this.updatePageViewsChart()
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

            // 初始化停留时间图表
            this.timeSpentChart = echarts.init(document.getElementById('timeSpentChart'))

            // 初始化内容类型图表
            this.contentTypeChart = echarts.init(document.getElementById('contentTypeChart'))

            // 初始化并更新图表数据
            this.updatePageViewsChart()
            this.updateTimeSpentChart()
            this.updateContentTypeChart()

            // 窗口大小变化时重新调整图表大小
            window.addEventListener('resize', () => {
                this.pageViewsChart.resize()
                this.timeSpentChart.resize()
                this.contentTypeChart.resize()
            })
        },
        updatePageViewsChart() {
            if (!this.pageViewsChart || !this.pageViewsData) return

            const option = {
                title: {
                    text: '页面访问统计',
                    left: 'center'
                },
                tooltip: {
                    trigger: 'axis',
                    axisPointer: {
                        type: 'shadow'
                    }
                },
                xAxis: {
                    type: 'category',
                    data: this.pageViewsData.map(item => item.page || '')
                },
                yAxis: {
                    type: 'value'
                },
                series: [{
                    data: this.pageViewsData.map(item => item.visits || 0),
                    type: 'bar'
                }]
            }

            this.pageViewsChart.setOption(option)
        },
        updateTimeSpentChart() {
            if (!this.timeSpentChart) return

            const option = {
                title: {
                    text: '用户停留时间'
                },
                tooltip: {
                    trigger: 'axis'
                },
                xAxis: {
                    type: 'category',
                    data: this.timeSpentData.map(item => item.page)
                },
                yAxis: {
                    type: 'value',
                    axisLabel: {
                        formatter: '{value} 秒'
                    }
                },
                series: [{
                    data: this.timeSpentData.map(item => item.timeSpent),
                    type: 'line'
                }]
            }

            this.timeSpentChart.setOption(option)
        },
        updateContentTypeChart() {
            if (!this.contentTypeChart || !this.contentTypeData) return

            const option = {
                title: {
                    text: '内容类型访问统计',
                    left: 'center'
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
                        data: this.contentTypeData.map(item => ({
                            name: item.type || '',
                            value: item.count || 0
                        })),
                        emphasis: {
                            itemStyle: {
                                shadowBlur: 10,
                                shadowOffsetX: 0,
                                shadowColor: 'rgba(0, 0, 0, 0.5)'
                            }
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
        }
    }
}
</script>