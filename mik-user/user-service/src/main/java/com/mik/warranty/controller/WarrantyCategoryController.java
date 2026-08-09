package com.mik.warranty.controller;

import com.mik.core.pojo.Result;
import com.mik.warranty.dto.WarrantyCategoryDTO;
import com.mik.warranty.entity.WarrantyCategory;
import com.mik.warranty.service.WarrantyCategoryService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("v1/warranty/category")
public class WarrantyCategoryController {

    @Resource
    private WarrantyCategoryService warrantyCategoryService;

    /**
     * 获取树形列表
     */
    @GetMapping("/tree")
    public Result<List<WarrantyCategoryDTO>> tree() {
        return Result.success(warrantyCategoryService.getTree());
    }

    /**
     * 获取小程序需要的分类数据（首页展示的一级分类 + 所有二级分类）
     */
    @GetMapping("/miniapp")
    public Result<Map<String, Object>> getMiniappCategories() {
        return Result.success(warrantyCategoryService.getMiniappCategories());
    }

    /**
     * 保存（新增/编辑）
     */
    @PostMapping("/save")
    public Result<Map<String, Long>> save(@RequestBody WarrantyCategory category) {
        Long id = warrantyCategoryService.saveCategory(category);
        Map<String, Long> result = new HashMap<>();
        result.put("id", id);
        return Result.success(result);
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    public Result delete(@RequestBody Map<String, Long> body) {
        warrantyCategoryService.delete(body.get("id"));
        return Result.success();
    }
}
