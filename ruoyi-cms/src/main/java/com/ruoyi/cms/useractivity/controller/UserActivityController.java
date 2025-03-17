package com.ruoyi.cms.useractivity.controller;

import com.ruoyi.cms.useractivity.domain.CmsUserActivity;
import com.ruoyi.cms.useractivity.service.ICmsUserActivityService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.core.domain.model.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.alibaba.fastjson.JSONObject;

import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.List;

/**
 * 用户行为跟踪 Controller
 */
@RestController
@RequestMapping("/cms/userActivity")
public class UserActivityController extends BaseController {

    @Autowired
    private ICmsUserActivityService userActivityService;

    /**
     * 记录用户行为数据
     */
    @PostMapping("/track")
    public AjaxResult trackActivity(@RequestBody CmsUserActivity activity, HttpServletRequest request) {
        try {
            LoginUser loginUser = getLoginUser();
            // 获取用户IP
            String ipAddress = IpUtils.getIpAddr(ServletUtils.getRequest());
            activity.setIpAddress(ipAddress);

            // 如果前端未传用户ID但用户已登录
            if (activity.getUserId() == null && loginUser != null) {
                activity.setUserId(loginUser.getUserId());
                activity.setUsername(loginUser.getUsername());
            }

            // 设置创建者
            if (loginUser != null) {
                activity.setCreateBy(loginUser.getUsername());
            } else {
                activity.setCreateBy("anonymous");
            }

            // 设置时间戳
            if (activity.getTimestamp() == null) {
                activity.setTimestamp(new Date());
            }

            // 设置创建时间
            activity.setCreateTime(new Date());

            // 获取用户信息
            String userAgent = request.getHeader("User-Agent");

            // 提取设备类型（而不是存储整个UA字符串）
            String deviceType = "Unknown";
            if (userAgent != null) {
                if (userAgent.contains("Mobile") || userAgent.contains("Android") || userAgent.contains("iPhone")) {
                    deviceType = "Mobile";
                } else if (userAgent.contains("iPad") || userAgent.contains("Tablet")) {
                    deviceType = "Tablet";
                } else {
                    deviceType = "Desktop";
                }
            }
            activity.setDevice(deviceType);

            // 提取浏览器信息
            String browserInfo = "Unknown";
            if (userAgent != null) {
                if (userAgent.contains("Chrome")) {
                    browserInfo = "Chrome";
                } else if (userAgent.contains("Firefox")) {
                    browserInfo = "Firefox";
                } else if (userAgent.contains("Safari")) {
                    browserInfo = "Safari";
                } else if (userAgent.contains("Edge")) {
                    browserInfo = "Edge";
                } else if (userAgent.contains("MSIE") || userAgent.contains("Trident")) {
                    browserInfo = "IE";
                }
            }
            activity.setBrowser(browserInfo);

            // 详细日志
            System.out.println("====================用户活动数据====================");
            System.out.println("用户ID: " + activity.getUserId());
            System.out.println("用户名: " + activity.getUsername());
            System.out.println("路径: " + activity.getPath());
            System.out.println("停留时间: " + activity.getTimeSpent() + "秒");
            System.out.println("内容类型: " + activity.getContentType());
            System.out.println("IP地址: " + ipAddress);
            System.out.println("==================================================");

            // 调用service存储数据
            int result = userActivityService.insertUserActivity(activity);
            System.out.println("数据库插入结果: " + result);

            return toAjax(result);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("处理用户活动数据时发生错误: " + e.getMessage());
            return AjaxResult.error("处理用户活动数据时发生错误: " + e.getMessage());
        }
    }

    /**
     * 获取用户行为统计数据
     */
    @GetMapping("/statistics")
    public AjaxResult getStatistics() {
        Map<String, Object> statistics = userActivityService.getUserActivityStatistics();
        System.out.println("==== 返回统计数据详情 ====");
        System.out.println(JSONObject.toJSONString(statistics));
        System.out.println("=======================");
        return AjaxResult.success(statistics);
    }

    /**
     * 获取热门浏览内容类型统计
     */
    @GetMapping("/popular-content-types")
    public AjaxResult getPopularContentTypes() {
        List<Map<String, Object>> contentTypes = userActivityService.getPopularContentTypes();
        System.out.println("==== 返回内容类型统计 ====");
        System.out.println(JSONObject.toJSONString(contentTypes));
        System.out.println("=======================");
        return AjaxResult.success(contentTypes);
    }

    /**
     * 获取热门搜索关键词
     */
    @GetMapping("/popular-keywords")
    public AjaxResult getPopularKeywords() {
        return AjaxResult.success(userActivityService.getPopularSearchKeywords());
    }

    /**
     * 获取用户平均停留时间
     */
    @GetMapping("/average-time")
    public AjaxResult getAverageTimeSpent() {
        return AjaxResult.success(userActivityService.getAverageTimeSpent());
    }

    /**
     * 测试插入用户行为数据
     */
    @GetMapping("/test-insert")
    public AjaxResult testInsert() {
        try {
            CmsUserActivity activity = new CmsUserActivity();
            activity.setUserId(1L);
            activity.setUsername("admin");
            activity.setPath("/test/path");
            activity.setTimeSpent(60L);
            activity.setContentType("测试页面");
            activity.setDevice("PC测试");
            activity.setBrowser("测试浏览器");
            activity.setIpAddress("127.0.0.1");
            activity.setTimestamp(new Date());
            activity.setCreateBy("test");
            activity.setCreateTime(new Date());

            int result = userActivityService.insertUserActivity(activity);
            System.out.println("测试插入结果: " + result);
            System.out.println("插入的用户活动: " + activity);

            return AjaxResult.success("测试插入结果: " + result);
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("测试插入失败: " + e.getMessage());
        }
    }
}