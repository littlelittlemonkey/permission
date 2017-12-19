package com.mmall.dao;

import com.mmall.beans.PageQuery;
import com.mmall.model.SysUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysUserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SysUser record);

    int insertSelective(SysUser record);

    SysUser selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SysUser record);

    int updateByPrimaryKey(SysUser record);
    //查找用户
    SysUser findByKeyword(@Param("keyword") String keyword);
    //邮箱校验
    int countByMail(@Param("mail") String mail, @Param("id") Integer id);
    //电话号码校验
    int countByTelephone(@Param("telephone") String telephone, @Param("id") Integer id);
    //查询是否存在此部门
    int countByDeptId(@Param("deptId") int deptId);
    //获取当前部门下的用户
    List<SysUser> getPageByDeptId(@Param("deptId") int deptId, @Param("page") PageQuery page);

    List<SysUser> getByIdList(@Param("idList") List<Integer> idList);

    List<SysUser> getAll();


}