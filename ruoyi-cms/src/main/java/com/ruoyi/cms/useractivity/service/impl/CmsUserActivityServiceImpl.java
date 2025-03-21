package com.ruoyi.cms.useractivity.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.cms.useractivity.domain.CmsUserActivity;
import com.ruoyi.cms.useractivity.service.ICmsUserActivityService;
import com.ruoyi.cms.useractivity.mapper.CmsUserActivityMapper;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.IpUtils;
import com.ruoyi.common.utils.ServletUtils;
import org.springframework.util.StringUtils;

/**
 * 用户行为统计 服务实现
 */
@Service
public class CmsUserActivityServiceImpl implements ICmsUserActivityService {
    @Autowired
    private CmsUserActivityMapper cmsUserActivityMapper; // 确保注入了Mapper

    // 临时存储用户活动数据，实际项目中应该使用数据库
    private static final List<CmsUserActivity> activityList = new ArrayList<>();

    /**
     * 插入用户行为数据
     * 
     * @param activity 用户行为数据
     * @return 结果
     */
    @Override
    public int insertUserActivity(CmsUserActivity userActivity) {
        // 记录一下收到的数据
        System.out.println("====================用户活动数据====================");
        System.out.println("用户ID: " + userActivity.getUserId());
        System.out.println("用户名: " + userActivity.getUsername());
        System.out.println("路径: " + userActivity.getPath());
        System.out.println("停留时间: " + userActivity.getTimeSpent() + "秒");
        System.out.println("内容类型: " + userActivity.getContentType());
        System.out.println("IP地址: " + userActivity.getIpAddress());
        System.out.println("==================================================");

        System.out.println("Service层接收到数据: " + userActivity.getUsername() + ", 路径: " + userActivity.getPath());

        try {
            // 截断device字段，确保不超过数据库列大小（假设数据库列为VARCHAR(100)）
            if (userActivity.getDevice() != null && userActivity.getDevice().length() > 100) {
                userActivity.setDevice(userActivity.getDevice().substring(0, 100));
            }

            // 截断browser字段
            if (userActivity.getBrowser() != null && userActivity.getBrowser().length() > 50) {
                userActivity.setBrowser(userActivity.getBrowser().substring(0, 50));
            }

            // 设置当前时间戳
            if (userActivity.getTimestamp() == null) {
                userActivity.setTimestamp(new Date());
            }

            // 确保时间值有效
            if (userActivity.getTimeSpent() == null) {
                userActivity.setTimeSpent(0L);
            }

            // 记录IP地址（如果还没设置）
            if (userActivity.getIpAddress() == null || userActivity.getIpAddress().isEmpty()) {
                userActivity.setIpAddress(IpUtils.getIpAddr(ServletUtils.getRequest()));
            }

            // 设置创建时间
            userActivity.setCreateTime(DateUtils.getNowDate());

            int result = cmsUserActivityMapper.insertCmsUserActivity(userActivity);
            System.out.println("数据库插入结果: " + result);
            return result;
        } catch (Exception e) {
            System.out.println("Service层插入用户活动数据失败: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 查询用户行为数据列表
     * 
     * @param activity 用户行为数据
     * @return 用户行为数据集合
     */
    @Override
    public List<CmsUserActivity> selectUserActivityList(CmsUserActivity activity) {
        return cmsUserActivityMapper.selectCmsUserActivityList(activity);
    }

    /**
     * 获取用户行为统计数据
     * 
     * @return 统计数据
     */
    @Override
    public Map<String, Object> getUserActivityStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        try {
            // 查询所有用户活动数据
            List<CmsUserActivity> activityList = cmsUserActivityMapper.selectCmsUserActivityList(new CmsUserActivity());
            System.out.println("查询到活动记录数量: " + activityList.size());

            if (activityList.isEmpty()) {
                statistics.put("pageViews", new ArrayList<>());
                statistics.put("totalVisits", 0);
                statistics.put("avgTimeSpent", 0);
                statistics.put("activeUsers", 0);
                System.out.println("活动记录为空，返回空数据");
                return statistics;
            }

            // 计算总访问量
            int totalVisits = activityList.size();

            // 计算活跃用户数(不同用户ID的数量)
            long activeUsers = activityList.stream()
                    .map(CmsUserActivity::getUserId)
                    .distinct()
                    .count();

            // 计算平均停留时间
            long totalTimeSpent = activityList.stream()
                    .mapToLong(CmsUserActivity::getTimeSpent)
                    .sum();
            long avgTimeSpent = activityList.isEmpty() ? 0 : totalTimeSpent / activityList.size();

            // 统计页面访问量
            Map<String, Integer> pageVisits = new HashMap<>();
            for (CmsUserActivity activity : activityList) {
                String path = activity.getPath();
                if (path != null) {
                    pageVisits.put(path, pageVisits.getOrDefault(path, 0) + 1);
                }
            }

            // 转换为前端需要的格式
            List<Map<String, Object>> pageViewsData = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : pageVisits.entrySet()) {
                Map<String, Object> item = new HashMap<>();
                item.put("page", entry.getKey());
                item.put("visits", entry.getValue());
                pageViewsData.add(item);
            }

            // 按访问量排序
            pageViewsData.sort((a, b) -> {
                Integer visitsA = (Integer) a.get("visits");
                Integer visitsB = (Integer) b.get("visits");
                return visitsB.compareTo(visitsA);
            });

            // 限制返回记录数
            if (pageViewsData.size() > 5) {
                pageViewsData = pageViewsData.subList(0, 5);
            }

            // 填充所有统计数据
            statistics.put("pageViews", pageViewsData);
            statistics.put("totalVisits", totalVisits);
            statistics.put("avgTimeSpent", avgTimeSpent);
            statistics.put("activeUsers", activeUsers);

            System.out.println("处理后的页面访问数据: " + pageViewsData);
            System.out.println("总访问量: " + totalVisits + ", 平均停留时间: " + avgTimeSpent + "秒, 活跃用户: " + activeUsers);

            return statistics;
        } catch (Exception e) {
            System.out.println("获取用户活动统计数据失败: " + e.getMessage());
            e.printStackTrace();
            return statistics;
        }
    }

    /**
     * 获取热门内容类型
     * 
     * @return 内容类型统计
     */
    @Override
    public List<Map<String, Object>> getPopularContentTypes() {
        try {
            // 查询所有用户活动数据
            List<CmsUserActivity> activityList = cmsUserActivityMapper.selectCmsUserActivityList(new CmsUserActivity());

            // 统计内容类型
            Map<String, Integer> contentTypeCounts = new HashMap<>();
            for (CmsUserActivity activity : activityList) {
                String contentType = activity.getContentType();
                if (contentType != null) {
                    contentTypeCounts.put(contentType, contentTypeCounts.getOrDefault(contentType, 0) + 1);
                }
            }

            // 转换为前端需要的格式
            List<Map<String, Object>> contentTypes = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : contentTypeCounts.entrySet()) {
                Map<String, Object> item = new HashMap<>();
                item.put("type", entry.getKey());
                item.put("count", entry.getValue());
                contentTypes.add(item);
            }

            // 按数量排序
            contentTypes.sort((a, b) -> {
                Integer countA = (Integer) a.get("count");
                Integer countB = (Integer) b.get("count");
                return countB.compareTo(countA);
            });

            return contentTypes;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 获取热门搜索关键词
     * 
     * @return 关键词统计
     */
    @Override
    public List<Map<String, Object>> getPopularSearchKeywords() {
        List<Map<String, Object>> keywords = new ArrayList<>();

        // 模拟搜索关键词数据
        Map<String, Object> keyword1 = new HashMap<>();
        keyword1.put("keyword", "考研政治");
        keyword1.put("count", 42);
        keyword1.put("percentage", 26.5);
        keywords.add(keyword1);

        Map<String, Object> keyword2 = new HashMap<>();
        keyword2.put("keyword", "数学");
        keyword2.put("count", 37);
        keyword2.put("percentage", 23.4);
        keywords.add(keyword2);

        Map<String, Object> keyword3 = new HashMap<>();
        keyword3.put("keyword", "英语");
        keyword3.put("count", 29);
        keyword3.put("percentage", 18.3);
        keywords.add(keyword3);

        Map<String, Object> keyword4 = new HashMap<>();
        keyword4.put("keyword", "院校排名");
        keyword4.put("count", 24);
        keyword4.put("percentage", 15.2);
        keywords.add(keyword4);

        Map<String, Object> keyword5 = new HashMap<>();
        keyword5.put("keyword", "参考书目");
        keyword5.put("count", 16);
        keyword5.put("percentage", 10.1);
        keywords.add(keyword5);

        return keywords;
    }

    /**
     * 获取页面平均停留时间
     * 
     * @return 停留时间统计
     */
    @Override
    public List<Map<String, Object>> getAverageTimeSpent() {
        // 查询真实数据库数据
        CmsUserActivity query = new CmsUserActivity();
        List<CmsUserActivity> activityList = cmsUserActivityMapper.selectCmsUserActivityList(query);

        // 按页面路径分组，计算平均停留时间
        Map<String, List<Long>> pathTimeMap = new HashMap<>();
        for (CmsUserActivity activity : activityList) {
            if (activity.getPath() != null && activity.getTimeSpent() != null) {
                if (!pathTimeMap.containsKey(activity.getPath())) {
                    pathTimeMap.put(activity.getPath(), new ArrayList<>());
                }
                pathTimeMap.get(activity.getPath()).add(activity.getTimeSpent());
            }
        }

        // 计算每个路径的平均停留时间
        List<Map<String, Object>> timeSpent = new ArrayList<>();
        for (Map.Entry<String, List<Long>> entry : pathTimeMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("page", entry.getKey());

            // 计算平均时间
            long sum = 0;
            for (Long time : entry.getValue()) {
                sum += time;
            }
            double average = entry.getValue().isEmpty() ? 0 : (double) sum / entry.getValue().size();

            item.put("timeSpent", (int) average); // 取整数秒
            timeSpent.add(item);
        }

        // 按平均时间排序
        timeSpent.sort((a, b) -> {
            Integer timeA = (Integer) a.get("timeSpent");
            Integer timeB = (Integer) b.get("timeSpent");
            return timeB.compareTo(timeA);
        });

        // 限制返回记录数
        if (timeSpent.size() > 5) {
            timeSpent = timeSpent.subList(0, 5);
        }

        return timeSpent;
    }

    /**
     * 获取内容类型统计
     */
    @Override
    public List<Map<String, Object>> getContentTypeStatistics() {
        List<Map<String, Object>> result = new ArrayList<>();

        // 查询各类型统计 - 替换this.list()调用
        List<CmsUserActivity> activities = cmsUserActivityMapper.selectCmsUserActivityList(new CmsUserActivity());

        // 统计各内容类型的访问量
        Map<String, Integer> typeCountMap = new HashMap<>();
        for (CmsUserActivity activity : activities) {
            String contentType = activity.getContentType();
            if (contentType == null || contentType.trim().isEmpty()) {
                contentType = "其他";
            }
            typeCountMap.put(contentType, typeCountMap.getOrDefault(contentType, 0) + 1);
        }

        // 转换为前端需要的格式
        for (Map.Entry<String, Integer> entry : typeCountMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", entry.getKey());
            item.put("count", entry.getValue());
            result.add(item);
        }

        return result;
    }

    /**
     * 获取用户停留时间统计
     */
    @Override
    public List<Map<String, Object>> getTimeSpentStatistics() {
        List<Map<String, Object>> result = new ArrayList<>();

        // 查询停留时间数据 - 替换this.list()调用
        List<CmsUserActivity> activities = cmsUserActivityMapper.selectCmsUserActivityList(new CmsUserActivity());

        // 按页面路径分组，计算每个页面的平均停留时间
        Map<String, List<Long>> pathTimeMap = new HashMap<>();
        for (CmsUserActivity activity : activities) {
            String path = activity.getPath();
            Long timeSpent = activity.getTimeSpent();

            if (timeSpent != null && timeSpent > 0) {
                if (!pathTimeMap.containsKey(path)) {
                    pathTimeMap.put(path, new ArrayList<>());
                }
                pathTimeMap.get(path).add(timeSpent);
            }
        }

        // 转换为前端需要的格式
        for (Map.Entry<String, List<Long>> entry : pathTimeMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("path", entry.getKey());

            List<Long> times = entry.getValue();
            long totalTime = 0;
            for (Long time : times) {
                totalTime += time;
            }
            int avgTime = times.size() > 0 ? (int) (totalTime / times.size()) : 0;

            item.put("timeSpent", avgTime);
            item.put("count", times.size());
            result.add(item);
        }

        return result;
    }
}