package com.study.room.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.study.room.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {}
