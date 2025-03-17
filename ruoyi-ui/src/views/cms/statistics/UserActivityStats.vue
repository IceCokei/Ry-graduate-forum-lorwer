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
            this.$http.get('/cms/userActivity/statistics').then(response => {
                const data = response.data
                // 初始化页面访问统计图表
                this.initPageViewsChart(data.pageViews || [])
                this.loading = false
            })

            // 获取内容类型统计
            this.$http.get('/cms/userActivity/popular-content').then(response => {
                // 初始化内容类型图表
                this.initContentTypeChart(response.data || [])
            })

            // 获取关键词统计
            this.$http.get('/cms/userActivity/popular-keywords').then(response => {
                this.keywordsData = response.data || []
            })

            // 获取停留时间统计
            this.$http.get('/cms/userActivity/average-time').then(response => {
                // 初始化停留时间图表
                this.initTimeSpentChart(response.data || [])
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
            if (!this.pageViewsChart) return

            const option = {
                title: {
                    text: '页面访问统计'
                },
                tooltip: {
                    trigger: 'axis'
                },
                xAxis: {
                    type: 'category',
                    data: this.pageViewsData.map(item => item.page)
                },
                yAxis: {
                    type: 'value'
                },
                series: [{
                    data: this.pageViewsData.map(item => item.count),
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
            if (!this.contentTypeChart) return

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
                    data: this.contentTypeData.map(item => item.type)
                },
                series: [
                    {
                        name: '访问次数',
                        type: 'pie',
                        radius: '55%',
                        center: ['50%', '60%'],
                        data: this.contentTypeData.map(item => ({
                            name: item.type,
                            value: item.count
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
            this.pageViewsData = data
            this.updatePageViewsChart()
        },
        initTimeSpentChart(data) {
            this.timeSpentData = data
            this.updateTimeSpentChart()
        },
        initContentTypeChart(data) {
            this.contentTypeData = data
            this.updateContentTypeChart()
        }
    }
}
</script>