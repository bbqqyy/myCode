package com.bqy.user_center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bqy.user_center.exception.BusinessException;
import com.bqy.user_center.model.domain.Team;
import com.bqy.user_center.model.domain.UserCenter;
import com.bqy.user_center.model.domain.UserTeam;
import com.bqy.user_center.model.domain.dto.TeamQuery;
import com.bqy.user_center.model.domain.enums.TeamStatusEnum;
import com.bqy.user_center.model.domain.request.TeamJoinRequest;
import com.bqy.user_center.model.domain.request.TeamQuitRequest;
import com.bqy.user_center.model.domain.request.TeamUpdateRequest;
import com.bqy.user_center.model.domain.vo.TeamUserVo;
import com.bqy.user_center.model.domain.vo.UserVo;
import com.bqy.user_center.service.TeamService;
import com.bqy.user_center.mapper.TeamMapper;
import com.bqy.user_center.service.UserCenterService;
import com.bqy.user_center.service.UserTeamService;
import com.bqy.user_center.utils.ErrorCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author 86173
 * @description 针对表【team(队伍)】的数据库操作Service实现
 * @createDate 2024-07-29 17:30:33
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

    @Resource
    private UserTeamService userTeamService;
    @Resource
    private UserCenterService userService;

    @Resource
    private RedissonClient redissonClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, UserCenter loginUser) {
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        final long userId = loginUser.getId();
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "登录用户ID错误");
        }
        int max = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (max < 1 || max > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不满足要求");
        }

        String name = team.getName();
        if (StringUtils.isBlank(name) || name.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍标题不满足要求");
        }

        String description = team.getDescription();
        if (StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述不满足要求");

        }

        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }
        String password = team.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            if (StringUtils.isBlank(password) || password.length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码设置不正确");
            }
        }

        if (new Date().after(team.getExpireTime())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "当前时间过期");
        }

        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        long hasTeamNum = this.count(queryWrapper);
        if (hasTeamNum >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多创建5个用户");
        }
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if (!result || teamId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }

        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        return teamId;


    }

    @Override
    public List<TeamUserVo> listTeams(TeamQuery teamQuery, boolean isAdmin) {
        //1.自己写SQL
        //查询队伍和创建人的信息
        //select * from team t left join user u on t.userId = u.id
        //查询队伍和已加入队伍成员的信息
        //select * from team t join user_team ut on t.id = ut.teamId
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        if (teamQuery != null) {
            Long id = teamQuery.getId();
            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }
            List<Long> idList = teamQuery.getIdList();
            if(CollectionUtils.isNotEmpty(idList)){
                queryWrapper.in("id",idList);
            }
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
            }

            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like("name", name);
            }
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.like("description", description);
            }
            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq("maxNum", maxNum);
            }
            Long userId = teamQuery.getUserId();
            if (userId != null && userId > 0) {
                queryWrapper.eq("userId", userId);
            }
            Integer status = teamQuery.getStatus();
            if (status != null && status > -1) {
                queryWrapper.eq("status", status);
            }
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
            if (statusEnum == null) {
                statusEnum = TeamStatusEnum.PUBLIC;
            }
            if (!isAdmin && statusEnum.equals(TeamStatusEnum.PRIVATE)) {
                throw new BusinessException(ErrorCode.NO_AUTH, "无权限");
            }
            queryWrapper.eq("status", statusEnum.getValue());

        }
        //不展示已过期队伍
        //expireTime isnull or expireTime
        queryWrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));
        List<Team> teamlist = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamlist)) {
            return new ArrayList<>();
        }
        List<TeamUserVo> teamUserVoList = new ArrayList<>();
        //关联查询创建人的用户信息

        for (Team team : teamlist) {
            Long useId = team.getUserId();
            if (useId == null) {
                continue;
            }
            UserCenter user = userService.getById(useId);
            TeamUserVo teamUserVo = new TeamUserVo();
            BeanUtils.copyProperties(team, teamUserVo);
            //脱敏用户信息
            if (user != null) {
                UserVo userVo = new UserVo();
                BeanUtils.copyProperties(user, userVo);
                teamUserVo.setCreateUser(userVo);
            }

            teamUserVoList.add(teamUserVo);
        }
        return teamUserVoList;

    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, UserCenter loginUser) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = teamUpdateRequest.getId();
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam = this.getById(id);
        if (oldTeam == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        if (oldTeam.getUserId() != loginUser.getId() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());

        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, updateTeam);
        if (teamStatusEnum.equals(TeamStatusEnum.SECRET)) {
            if (TeamStatusEnum.getEnumByValue(updateTeam.getStatus()).equals(TeamStatusEnum.PUBLIC)) {
                updateTeam.setPassword(null);
            }
            if (StringUtils.isBlank(teamUpdateRequest.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密房间必须要有密码");
            }
        }
        boolean result = this.updateById(updateTeam);

        return result;

    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, UserCenter loginUser) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long teamId = teamJoinRequest.getTeamId();
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }
        if (team.getExpireTime() != null && team.getExpireTime().before(new Date())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }
        Integer status = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if (TeamStatusEnum.PRIVATE.equals(teamStatusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "禁止加入私有队伍");
        }
        String password = teamJoinRequest.getPassword();
        if (TeamStatusEnum.SECRET.equals(teamStatusEnum)) {
            if (StringUtils.isBlank(password) || !password.equals(team.getPassword())) {
                throw new BusinessException(ErrorCode.NULL_ERROR, "密码错误");
            }
        }
        //该用户已加入的队伍数量
        long userId = loginUser.getId();
        RLock rLock = redissonClient.getLock("bqy:join_team");
        //分布式锁
        try {
            while (true){
                if(rLock.tryLock(0,-1, TimeUnit.MILLISECONDS)){
                    System.out.println("getLock:"+Thread.currentThread().getId());
                    QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("userId", userId);
                    long hasJoinNum = userTeamService.count(userTeamQueryWrapper);
                    if (hasJoinNum >= 5) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多加入五个队伍");
                    }
//        //不允许加入自己的队伍
//        if(team.getUserId()==userId){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR,"不允许加入自己的队伍");
//        }
                    //不能重复加入已加入的队伍
                    userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("userId", userId);
                    userTeamQueryWrapper.eq("teamId", teamId);
                    long hasUserJoinTeam = userTeamService.count(userTeamQueryWrapper);
                    if (hasUserJoinTeam > 0) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已加入该队伍");
                    }
                    //已加入队伍人数
                    long teamHasJoinNum = countTeamUserByTeamId(teamId);
                    if (teamHasJoinNum >= team.getMaxNum()) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已满");
                    }
                    //修改队伍信息
                    UserTeam userTeam = new UserTeam();
                    userTeam.setUserId(userId);
                    userTeam.setTeamId(teamId);
                    userTeam.setJoinTime(new Date());
                    boolean result = userTeamService.save(userTeam);
                    return result;
                }
            }

        }catch (InterruptedException e){
            log.error("doCacheRecommendUser error",e);
            return false;
        }finally {
            if(rLock.isHeldByCurrentThread()){
                System.out.println("unlock:"+Thread.currentThread().getId());
                rLock.unlock();
            }
        }

    }

    @Override
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, UserCenter loginUser) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamQuitRequest.getTeamId();
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }
        long userId = loginUser.getId();
        UserTeam queryUserTeam = new UserTeam();
        queryUserTeam.setTeamId(teamId);
        queryUserTeam.setUserId(userId);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>(queryUserTeam);
        long count = userTeamService.count(queryWrapper);
        if (count == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未加入队伍");
        }
        long hasJoinNum = this.countTeamUserByTeamId(teamId);
        if (hasJoinNum == 1) {
            //删除队伍和所有队伍的关系
            this.removeById(teamId);
        } else {
            if (team.getUserId() == userId) {
                QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("teamId", teamId);
                userTeamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam nextUserTeam = userTeamList.get(1);
                Long nextTeamLeaderId = nextUserTeam.getUserId();
                //更新当前队伍的队长
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextTeamLeaderId);
                boolean result = this.updateById(updateTeam);
                if (!result) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "更新队长失败");
                }
            }
        }
        //移除关系
        return userTeamService.remove(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(Long id,UserCenter loginUser) {
        Team team = this.getById(id);
        Long teamId = team.getId();
        if(team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍不存在");
        }
        if(!team.getUserId().equals(loginUser.getId()) ){
            throw new BusinessException(ErrorCode.NO_AUTH,"无访问权限");
        }
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();

        userTeamQueryWrapper.eq("teamId", teamId);
        boolean result = userTeamService.remove(userTeamQueryWrapper);
        if(!result){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"删除队伍关联信息失败");
        }

        result = this.removeById(teamId);
        return result;


    }

    /**
     * 获取某队伍当前人数
     *
     * @param teamId
     * @return
     */
    private long countTeamUserByTeamId(long teamId) {
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", teamId);
        return userTeamService.count(queryWrapper);
    }
}




