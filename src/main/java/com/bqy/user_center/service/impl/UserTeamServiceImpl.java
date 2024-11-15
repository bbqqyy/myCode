package com.bqy.user_center.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bqy.user_center.model.domain.UserTeam;
import com.bqy.user_center.service.UserTeamService;
import com.bqy.user_center.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author 86173
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2024-07-29 17:38:22
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




