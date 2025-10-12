package com.mik.user.mapper;

import com.mik.user.controller.cqe.PermissionDTO;
import com.mik.user.entity.Permission;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    List<PermissionDTO> listUserPermission(Long userId);

}
