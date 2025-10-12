package com.mik.user.service;

import com.mik.core.pojo.PageInput;
import com.mik.core.pojo.PageResult;
import com.mik.core.user.UserAuthService;
import com.mik.core.user.UserInfo;
import com.mik.user.dto.UserCreateDTO;
import com.mik.user.dto.UserDTO;
import com.mik.user.dto.UserListDTO;
import com.mik.user.dto.UserQuery;
import com.mik.user.entity.User;
import com.mik.user.entity.UserRole;
import com.mik.user.mapper.UserMapper;
import com.mik.user.mapper.UserRoleMapper;
import com.mik.user.utils.PageUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService extends ServiceImpl<UserMapper, User> implements UserAuthService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private PasswordEncoder encoder;

    @Value("${server.port}")
    private Integer port;


    public PageResult<UserListDTO> listByConditionPage(UserQuery query, PageInput page){
        Page<User> paginate = Page.of(page.getPageNum(), page.getPageSize());
        QueryCondition condition =  QueryCondition.create(new QueryColumn("username"), "like", "%" + query.getUsername() + "%");
        QueryWrapper wrapper = QueryWrapper.create().select().from("user").where(condition);

        Page<User> userListDTOS = userMapper.paginateAs(paginate, wrapper, User.class);

        Page<UserListDTO> map = userListDTOS.map(x -> {
            UserListDTO userListDTO = new UserListDTO();
            BeanUtils.copyProperties(x, userListDTO);
            List<UserRole> userRoles = userRoleMapper.selectListByCondition(QueryCondition.create(new QueryColumn("user_id"), "=", x.getUserId()));
            List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList());
            userListDTO.setRoleIds(roleIds);
            return userListDTO;
        });

        return PageUtil.transform(map);
    }

    public UserDTO getUserById(Long userId) {
        User user = userMapper.selectOneWithRelationsById(userId);
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getUserId());
        userDTO.setAge(18);
        userDTO.setEmail(user.getEmail());
        userDTO.setName(user.getUsername());
        userDTO.setPort(port);
        return userDTO;
    }

    public UserInfo getUserByIdentify(String identify) {
        QueryCondition condition =  QueryCondition.create(new QueryColumn("mobile"), "=", identify)
                .or(QueryCondition.create(new QueryColumn("email"), "=", identify))
                .or(QueryCondition.create(new QueryColumn("username"), "=", identify));
        QueryWrapper wrapper = QueryWrapper.create().select().from("user").where(condition);
        User user = userMapper.selectOneByQueryAs(wrapper, User.class);
        if(user == null){
            return null;
        }
        UserInfo userListDTO = new UserInfo();
        BeanUtils.copyProperties(user, userListDTO);
        return userListDTO;
    }

    public void createUser(UserCreateDTO createDTO) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        User user = new User();
        if(createDTO.getUserId() != null){
            user.setUserId(createDTO.getUserId());
        }
        BeanUtils.copyProperties(createDTO, user);
        if(createDTO.getUserId() == null){
            user.setPassword(encoder.encode("123456"));
        }

        userMapper.insertOrUpdateSelective(user);

        if(createDTO.getUserId() != null){
            userRoleMapper.deleteByCondition(QueryCondition.create(new QueryColumn("user_id"), "=", createDTO.getUserId()));
        }
        List<UserRole> userRoles = new ArrayList<>();
        if(StringUtils.isNotEmpty(createDTO.getRoleIds())){
            Arrays.stream(createDTO.getRoleIds().split(",")).forEach(roleId -> {
                UserRole userRole = new UserRole();
                userRole.setUserId(user.getUserId());
                userRole.setRoleId(Long.valueOf(roleId));
                userRoles.add(userRole);
            });
        }
        userRoleMapper.insertBatch(userRoles);
    }

    public void delUser(Long userId) {
        userMapper.deleteById(userId);
    }

    public void changeEnable(Long userId, Integer enable) {
        User user = userMapper.selectOneWithRelationsById(userId);
        user.setEnable(enable);
        userMapper.update(user);
    }
}
