package com.ruoyi.cms.useractivity.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.cms.useractivity.domain.CmsUserActivity;

/**
 * 用户行为统计 服务接口
 */
public interface ICmsUserActivityService {
    /**
     * 插入用户行为数据
     * 
     * @param activity 用户行为数据
     * @return 结果
     */
    public int insertUserActivity(CmsUserActivity activity);
    
    /**
     * 查询用户行为数据列表
     * 
     * @param activity 用户行为数据
     * @return 用户行为数据集合
     */
    public List<CmsUserActivity> selectUserActivityList(CmsUserActivity activity);
    
    /**
     * 获取用户行为统计数据
     * 
     * @return 统计数据
     */
    public Map<String, Object> getUserActivityStatistics();
    
    /**
     * 获取热门内容类型
     * 
     * @return 内容类型统计
     */
    public List<Map<String, Object>> getPopularContentTypes();
    
    /**
     * 获取热门搜索关键词
     * 
     * @return 关键词统计
     */
    public List<Map<String, Object>> getPopularSearchKeywords();
    
    /**
     * 获取页面平均停留时间
     * 
     * @return 停留时间统计
     */
    public List<Map<String, Object>> getAverageTimeSpent();
} 