package com.mik.user.mapper;

import com.mik.user.controller.cqe.RoleDTO;
import com.mik.user.entity.Role;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    List<RoleDTO> listUserRoles(Long userId);

}
