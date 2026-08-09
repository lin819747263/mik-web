package com.mik.warranty.service;

import com.mik.core.exception.ServiceException;
import com.mik.warranty.dto.WarrantyCategoryDTO;
import com.mik.warranty.entity.WarrantyCategory;
import com.mik.warranty.mapper.WarrantyCategoryMapper;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class WarrantyCategoryService extends ServiceImpl<WarrantyCategoryMapper, WarrantyCategory> {

    /**
     * 获取树形列表
     */
    public List<WarrantyCategoryDTO> getTree() {
        List<WarrantyCategory> all = getMapper().selectListByQuery(
                QueryWrapper.create().orderBy("sort asc"));
        List<WarrantyCategoryDTO> dtos = all.stream().map(item -> {
            WarrantyCategoryDTO dto = new WarrantyCategoryDTO();
            BeanUtils.copyProperties(item, dto);
            return dto;
        }).collect(Collectors.toList());

        return buildTree(dtos, 0L);
    }

    /**
     * 保存（新增/编辑）
     */
    public Long saveCategory(WarrantyCategory category) {
        // 校验名称
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new ServiceException("问题名称不能为空");
        }

        // 设置层级
        if (category.getParentId() == null || category.getParentId() == 0) {
            category.setParentId(0L);
            category.setLevel(1);
        } else {
            category.setLevel(2);
            // 二级问题不能设置部门和首页展示
            category.setDeptId(null);
            category.setDeptName(null);
            category.setShowOnHome(0);
        }

        if (category.getEnable() == null) {
            category.setEnable(1);
        }

        // 校验首页展示数量不超过8个
        if (category.getShowOnHome() != null && category.getShowOnHome() == 1) {
            checkShowOnHomeLimit(category.getId());
        }

        if (category.getId() == null) {
            getMapper().insert(category);
        } else {
            getMapper().update(category);
        }

        return category.getId();
    }

    /**
     * 删除
     */
    public void delete(Long id) {
        // 检查是否有子分类
        Long childCount = getMapper().selectCountByQuery(
                QueryWrapper.create().where(new QueryColumn("parent_id").eq(id)));
        if (childCount > 0) {
            throw new ServiceException("请先删除子分类");
        }

        getMapper().deleteById(id);
    }

    /**
     * 获取小程序需要的分类数据
     * 返回格式：{ categories: [...], subCategories: { ... } }
     */
    public Map<String, Object> getMiniappCategories() {
        // 获取所有启用的分类
        List<WarrantyCategory> all = getMapper().selectListByQuery(
                QueryWrapper.create()
                        .where(new QueryColumn("enable").eq(1))
                        .orderBy("sort asc"));

        // 一级分类：优先返回设置了首页展示的，如果没有则返回所有一级分类
        List<WarrantyCategory> level1List = all.stream()
                .filter(c -> c.getLevel() == 1)
                .collect(Collectors.toList());

        boolean hasShowOnHome = level1List.stream()
                .anyMatch(c -> c.getShowOnHome() != null && c.getShowOnHome() == 1);

        List<Map<String, Object>> categories = level1List.stream()
                .filter(c -> !hasShowOnHome || (c.getShowOnHome() != null && c.getShowOnHome() == 1))
                .map(c -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", String.valueOf(c.getId()));
                    map.put("name", c.getName());
                    map.put("dept", c.getDeptName() != null ? c.getDeptName() : "");
                    return map;
                })
                .collect(Collectors.toList());

        // 二级分类（按父ID分组）
        Map<String, List<String>> subCategories = all.stream()
                .filter(c -> c.getLevel() == 2)
                .collect(Collectors.groupingBy(
                        c -> String.valueOf(c.getParentId()),
                        Collectors.mapping(WarrantyCategory::getName, Collectors.toList())
                ));

        Map<String, Object> result = new HashMap<>();
        result.put("categories", categories);
        result.put("subCategories", subCategories);
        return result;
    }

    /**
     * 校验首页展示数量不超过8个
     */
    private void checkShowOnHomeLimit(Long excludeId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(new QueryColumn("show_on_home").eq(1))
                .and(new QueryColumn("level").eq(1));

        // 编辑时排除自身
        if (excludeId != null) {
            wrapper.and(new QueryColumn("id").ne(excludeId));
        }

        Long count = getMapper().selectCountByQuery(wrapper);
        if (count >= 8) {
            throw new ServiceException("首页展示最多设置8个一级问题");
        }
    }

    /**
     * 构建树
     */
    private List<WarrantyCategoryDTO> buildTree(List<WarrantyCategoryDTO> all, Long parentId) {
        List<WarrantyCategoryDTO> tree = new ArrayList<>();
        Map<Long, List<WarrantyCategoryDTO>> parentMap = all.stream()
                .collect(Collectors.groupingBy(WarrantyCategoryDTO::getParentId));

        List<WarrantyCategoryDTO> children = parentMap.getOrDefault(parentId, new ArrayList<>());
        for (WarrantyCategoryDTO child : children) {
            child.setChildren(buildTree(all, child.getId()));
            tree.add(child);
        }

        return tree;
    }
}
