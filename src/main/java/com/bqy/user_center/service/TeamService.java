package com.bqy.user_center.service;

import com.bqy.user_center.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bqy.user_center.model.domain.UserCenter;
import com.bqy.user_center.model.domain.dto.TeamQuery;
import com.bqy.user_center.model.domain.request.TeamJoinRequest;
import com.bqy.user_center.model.domain.request.TeamQuitRequest;
import com.bqy.user_center.model.domain.request.TeamUpdateRequest;
import com.bqy.user_center.model.domain.vo.TeamUserVo;

import java.util.List;

/**
* @author 86173
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-07-29 17:30:33
*/
public interface TeamService extends IService<Team> {
    /**
     * 创建队伍
     * @param team
     * @param userCenter
     * @return
     */
    long addTeam(Team team, UserCenter userCenter);

    /**
     * 搜索队伍
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    List<TeamUserVo> listTeams(TeamQuery teamQuery, boolean isAdmin);

    /**
     * 更新队伍
     * @param teamUpdateRequest
     * @return
     */

    boolean updateTeam(TeamUpdateRequest teamUpdateRequest,UserCenter loginUser);

    /**
     * 加入队伍
     * @param teamJoinRequest
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest,UserCenter loginUser);

    /**
     * 退出队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */

    boolean quitTeam(TeamQuitRequest teamQuitRequest, UserCenter loginUser);

    /**
     * 解散队伍
     * @param id
     * @return
     */
    boolean deleteTeam(Long id,UserCenter loginUser);
}
