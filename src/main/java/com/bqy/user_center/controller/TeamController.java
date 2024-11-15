package com.bqy.user_center.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bqy.user_center.common.DeleteRequest;
import com.bqy.user_center.exception.BusinessException;
import com.bqy.user_center.model.domain.Team;
import com.bqy.user_center.model.domain.UserCenter;
import com.bqy.user_center.model.domain.UserTeam;
import com.bqy.user_center.model.domain.dto.TeamQuery;
import com.bqy.user_center.model.domain.request.TeamAddRequest;
import com.bqy.user_center.model.domain.request.TeamJoinRequest;
import com.bqy.user_center.model.domain.request.TeamQuitRequest;
import com.bqy.user_center.model.domain.request.TeamUpdateRequest;
import com.bqy.user_center.model.domain.vo.TeamUserVo;
import com.bqy.user_center.model.domain.vo.UserVo;
import com.bqy.user_center.service.TeamService;
import com.bqy.user_center.service.UserCenterService;
import com.bqy.user_center.service.UserTeamService;
import com.bqy.user_center.utils.BaseResponse;
import com.bqy.user_center.utils.ErrorCode;
import com.bqy.user_center.utils.ResultUtils;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/team")
@Api(tags = "队伍管理模块")
@CrossOrigin(origins = {"http://localhost:3000"})
public class TeamController {

    @Resource
    private TeamService teamService;

    @Resource
    private UserCenterService userCenterService;

    @Resource
    private UserTeamService userTeamService;

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserCenter loginUser = userCenterService.LoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        long teamId = teamService.addTeam(team, loginUser);
        return ResultUtils.success(teamId);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        UserCenter loginUser = userCenterService.LoginUser(request);
        boolean result = teamService.deleteTeam(id, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "删除失败");
        }
        return ResultUtils.success(true);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserCenter loginUser = userCenterService.LoginUser(request);
        boolean result = teamService.updateTeam(teamUpdateRequest, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "更新失败");
        }
        return ResultUtils.success(true);
    }

    @GetMapping("/list")
    public BaseResponse<List<TeamUserVo>> listTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userCenterService.isAdmin(request);
        // 1、查询队伍列表
        List<TeamUserVo> teamList = teamService.listTeams(teamQuery, isAdmin);
        final List<Long> teamIdList = teamList.stream().map(TeamUserVo::getId).collect(Collectors.toList());
        // 2、判断当前用户是否已加入队伍
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        try {
            UserCenter loginUser = userCenterService.LoginUser(request);
            userTeamQueryWrapper.eq("userId", loginUser.getId());
            userTeamQueryWrapper.in("teamId", teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
            // 已加入的队伍 id 集合
            Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            teamList.forEach(team -> {
                boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
                team.setHasJoin(hasJoin);
            });
        } catch (Exception e) {
        }
        // 3、查询已加入队伍的人数
        QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
        userTeamJoinQueryWrapper.in("teamId", teamIdList);
        List<UserTeam> userTeamList = userTeamService.list(userTeamJoinQueryWrapper);
        // 队伍 id => 加入这个队伍的用户列表
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(team -> team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size()));
        return ResultUtils.success(teamList);
    }

    @GetMapping("/get")
    public BaseResponse<Team> getTeam(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return ResultUtils.success(team);
    }

    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVo>> ListMyCreateTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserCenter loginUser = userCenterService.LoginUser(request);
        teamQuery.setUserId(loginUser.getId());
        List<TeamUserVo> teamList = teamService.listTeams(teamQuery, true);
        return ResultUtils.success(teamList);

    }

    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamByPage(TeamQuery teamQuery) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        Page<Team> page = new Page(teamQuery.getPageNum(), teamQuery.getPageSize());
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> teamPage = teamService.page(page, queryWrapper);
        return ResultUtils.success(teamPage);

    }

    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserCenter loginUser = userCenterService.LoginUser(request);
        boolean result = teamService.joinTeam(teamJoinRequest, loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserCenter loginUser = userCenterService.LoginUser(request);
        boolean result = teamService.quitTeam(teamQuitRequest, loginUser);
        return ResultUtils.success(result);
    }

    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVo>> listMyJoinTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserCenter loginUser = userCenterService.LoginUser(request);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        Map<Long, List<UserTeam>> listMap = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        List<Long> idList = new ArrayList<>(listMap.keySet());
        teamQuery.setIdList(idList);
        List<TeamUserVo> teamUserVoList = teamService.listTeams(teamQuery, true);
        return ResultUtils.success(teamUserVoList);
    }


}
