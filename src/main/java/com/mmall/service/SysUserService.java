package com.mmall.service;

import com.google.common.base.Preconditions;
import com.mmall.beans.PageQuery;
import com.mmall.beans.PageResult;
import com.mmall.common.RequestHolder;
import com.mmall.dao.SysUserMapper;
import com.mmall.exception.ParamException;
import com.mmall.model.SysUser;
import com.mmall.param.UserParam;
import com.mmall.utill.BeanValidator;
import com.mmall.utill.IpUtil;
import com.mmall.utill.MD5Util;
import com.mmall.utill.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.ParameterMetaData;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;


    public void save(UserParam param) {
        BeanValidator.check(param);
        if(checkTelephoneExist(param.getTelephone(), param.getId())) {
            throw new ParamException("电话已被占用");
        }
        if(checkEmailExist(param.getMail(), param.getId())) {
            throw new ParamException("邮箱已被占用");
        }
        String password = PasswordUtil.randomPassword();
        //TODO:
        password = "123456";
        String encryptedPassword = MD5Util.encrypt(password);
//        SysUser user = SysUser.builder().username(param.getUsername()).telephone(param.getTelephone()).mail(param.getMail())
//                .password(encryptedPassword).deptId(param.getDeptId()).status(param.getStatus()).remark(param.getRemark()).build();
        SysUser user = new SysUser();
        user.setUsername(param.getUsername());
        user.setTelephone(param.getTelephone());
        user.setMail(param.getMail());
        user.setPassword(encryptedPassword);
        user.setDeptId(param.getDeptId());
        user.setStatus(param.getStatus());
        user.setRemark(param.getRemark());
        user.setOperator(RequestHolder.getCurrentUser().getUsername());
        user.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        user.setOperateTime(new Date());

        // TODO: sendEmail

        sysUserMapper.insertSelective(user);
//        sysLogService.saveUserLog(null, user);
    }

    /**
     * 更新操作
     * 根据用户id更新
     * @param param
     */
    public void update(UserParam param) {
        BeanValidator.check(param);
        if(checkTelephoneExist(param.getTelephone(), param.getId())) {
            throw new ParamException("电话已被占用");
        }
        if(checkEmailExist(param.getMail(), param.getId())) {
            throw new ParamException("邮箱已被占用");
        }
        SysUser before = sysUserMapper.selectByPrimaryKey(param.getId());
        Preconditions.checkNotNull(before, "待更新的用户不存在");
//        SysUser after = SysUser.builder().id(param.getId()).username(param.getUsername()).telephone(param.getTelephone()).mail(param.getMail())
//                .deptId(param.getDeptId()).status(param.getStatus()).remark(param.getRemark()).build();
        SysUser after = new SysUser();
        after.setId(param.getId());
        after.setUsername(param.getUsername());
        after.setTelephone(param.getTelephone());
        after.setMail(param.getMail());
        after.setDeptId(param.getDeptId());
        after.setStatus(param.getStatus());
        after.setRemark(param.getRemark());
        after.setOperator(RequestHolder.getCurrentUser().getUsername());
        after.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        after.setOperateTime(new Date());
        sysUserMapper.updateByPrimaryKeySelective(after);
//        sysLogService.saveUserLog(before, after);
    }

    /**
     * 获取当前部门的用户，分页
     * @param deptId
     * @param page
     * @return
     */
    public PageResult<SysUser> getPageByDeptId(int deptId, PageQuery page) {
        BeanValidator.check(page);
        int count = sysUserMapper.countByDeptId(deptId);
        if (count > 0) {
            //获取当前部门下的用户，并分页展示
            List<SysUser> list = sysUserMapper.getPageByDeptId(deptId, page);
            return PageResult.<SysUser>builder().total(count).data(list).build();
        }
        return PageResult.<SysUser>builder().build();
    }

    public boolean checkEmailExist(String mail, Integer userId) {
        return sysUserMapper.countByMail(mail, userId) > 0;
    }

    public boolean checkTelephoneExist(String telephone, Integer userId) {
        return sysUserMapper.countByTelephone(telephone, userId) > 0;
    }
    /**
     * 查找用户
     * @param keyword
     * @return
     */
    public SysUser findByKeyword(String keyword){
        return sysUserMapper.findByKeyword(keyword);
    }
}
