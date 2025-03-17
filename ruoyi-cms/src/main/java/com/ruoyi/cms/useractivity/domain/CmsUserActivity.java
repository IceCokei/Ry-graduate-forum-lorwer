package com.ruoyi.cms.useractivity.domain;

import java.util.Date;
import java.util.Map;
import java.util.List;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 用户行为数据对象 cms_user_activity
 */
public class CmsUserActivity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 活动ID */
    private Long activityId;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 页面路径 */
    private String path;

    /** 页面停留时间(秒) */
    private Long timeSpent;

    /** 页面访问次数 - 不存储到数据库 */
    private transient Map<String, Integer> pageViews;

    /** 搜索关键词 - 不存储到数据库 */
    private transient List<Map<String, Object>> searchKeywords;

    /** 内容类型 */
    private String contentType;

    /** 访问设备 */
    private String device;

    /** 浏览器信息 */
    private String browser;

    /** IP地址 */
    private String ipAddress;

    /** 活动时间戳 */
    private Date timestamp;

    // 如果不使用@Transient，也可以添加JSON序列化字段
    private String pageViewsJson;
    private String searchKeywordsJson;

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(Long timeSpent) {
        this.timeSpent = timeSpent;
    }

    public Map<String, Integer> getPageViews() {
        return pageViews;
    }

    public void setPageViews(Map<String, Integer> pageViews) {
        this.pageViews = pageViews;
    }

    public List<Map<String, Object>> getSearchKeywords() {
        return searchKeywords;
    }

    public void setSearchKeywords(List<Map<String, Object>> searchKeywords) {
        this.searchKeywords = searchKeywords;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getPageViewsJson() {
        return pageViewsJson;
    }

    public void setPageViewsJson(String pageViewsJson) {
        this.pageViewsJson = pageViewsJson;
    }

    public String getSearchKeywordsJson() {
        return searchKeywordsJson;
    }

    public void setSearchKeywordsJson(String searchKeywordsJson) {
        this.searchKeywordsJson = searchKeywordsJson;
    }
}