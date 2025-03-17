package com.ruoyi.cms.useractivity.mapper;

import java.util.List;
import com.ruoyi.cms.useractivity.domain.CmsUserActivity;

/**
 * 用户行为记录Mapper接口
 */
public interface CmsUserActivityMapper {
    /**
     * 查询用户行为记录
     *
     * @param activityId 用户行为记录ID
     * @return 用户行为记录
     */
    public CmsUserActivity selectCmsUserActivityById(Long activityId);

    /**
     * 查询用户行为记录列表
     *
     * @param cmsUserActivity 用户行为记录
     * @return 用户行为记录集合
     */
    public List<CmsUserActivity> selectCmsUserActivityList(CmsUserActivity cmsUserActivity);

    /**
     * 新增用户行为记录
     *
     * @param cmsUserActivity 用户行为记录
     * @return 结果
     */
    public int insertCmsUserActivity(CmsUserActivity cmsUserActivity);

    /**
     * 修改用户行为记录
     *
     * @param cmsUserActivity 用户行为记录
     * @return 结果
     */
    public int updateCmsUserActivity(CmsUserActivity cmsUserActivity);

    /**
     * 删除用户行为记录
     *
     * @param activityId 用户行为记录ID
     * @return 结果
     */
    public int deleteCmsUserActivityById(Long activityId);

    /**
     * 批量删除用户行为记录
     *
     * @param activityIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteCmsUserActivityByIds(String[] activityIds);
} 