package com.mmall.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.mmall.dao.SysAclModuleMapper;
import com.mmall.dao.SysDeptMapper;
import com.mmall.dto.DeptLevelDto;
import com.mmall.exception.ParamException;
import com.mmall.model.SysAclModule;
import com.mmall.model.SysDept;
import com.mmall.param.AclModuleLevelDto;
import com.mmall.utill.LevelUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class SysTreeService {

    @Autowired
    private SysDeptMapper sysDeptMapper;
    @Autowired
    private SysAclModuleMapper sysAclModuleMapper;

    /**
     * 部门树
     *
     * @return
     */
    public List<DeptLevelDto> deptTree() {
        List<SysDept> deptList = sysDeptMapper.getAllDept();//获取所有部门

        List<DeptLevelDto> dtoList = Lists.newArrayList();
        //将部门list转化为前台易操作的DeptLevelDto list
        for (SysDept dept : deptList) {
            DeptLevelDto dto = DeptLevelDto.adapt(dept);
            dtoList.add(dto);
        }
        //返回部门list
        return deptListToTree(dtoList);
    }

    /**
     * 将部门list转化为树
     *
     * @param deptLevelList
     * @return
     */
    public List<DeptLevelDto> deptListToTree(List<DeptLevelDto> deptLevelList) {
        if (CollectionUtils.isEmpty(deptLevelList)) {
            return Lists.newArrayList();
        }
        // level -> [dept1, dept2, ...] Map<String, List<Object>>
        //部门树结构
        Multimap<String, DeptLevelDto> levelDeptMap = ArrayListMultimap.create();
        //存放根节点
        List<DeptLevelDto> rootList = Lists.newArrayList();
        //区分当前节点和后代节点
        for (DeptLevelDto dto : deptLevelList) {
            levelDeptMap.put(dto.getLevel(), dto);
            if (LevelUtil.ROOT.equals(dto.getLevel())) {
                rootList.add(dto);
            }
        }
        // 按照seq从小到大排序
        Collections.sort(rootList, new Comparator<DeptLevelDto>() {
            public int compare(DeptLevelDto o1, DeptLevelDto o2) {
                return o1.getSeq() - o2.getSeq();
            }
        });
        // 递归生成树
        transformDeptTree(rootList, LevelUtil.ROOT, levelDeptMap);
        return rootList;
    }

    // level:0, 0, all 0->0.1,0.2
    // level:0.1
    // level:0.2

    /**
     * 递归遍历树结构
     *
     * @param deptLevelList 当前部门list
     * @param level         当前level [0.1.2]
     * @param levelDeptMap  需要遍历的<level,dept>
     */
    public void transformDeptTree(List<DeptLevelDto> deptLevelList, String level, Multimap<String, DeptLevelDto> levelDeptMap) {
        for (int i = 0; i < deptLevelList.size(); i++) {
            // 遍历该层的每个元素
            DeptLevelDto deptLevelDto = deptLevelList.get(i);
            // 处理当前层级的数据
            String nextLevel = LevelUtil.calculateLevel(level, deptLevelDto.getId());
            // 处理下一层
            List<DeptLevelDto> tempDeptList = (List<DeptLevelDto>) levelDeptMap.get(nextLevel);
            if (CollectionUtils.isNotEmpty(tempDeptList)) {
                // 排序
                Collections.sort(tempDeptList, new Comparator<DeptLevelDto>() {
                            public int compare(DeptLevelDto o1, DeptLevelDto o2) {
                                return o1.getSeq() - o2.getSeq();
                            }
                        }
                );
                // 设置下一层部门
                deptLevelDto.setDeptList(tempDeptList);
                // 进入到下一层处理
                transformDeptTree(tempDeptList, nextLevel, levelDeptMap);
            }
        }
    }

    //    -----------------------------------

    /**
     * 获取权限模块树
     * @return
     */
    public List<AclModuleLevelDto> aclModuleTree() {
        List<SysAclModule> aclModuleList = sysAclModuleMapper.getAllAclModule();
        List<AclModuleLevelDto> dtoList = Lists.newArrayList();
        for (SysAclModule aclModule : aclModuleList) {
            dtoList.add(AclModuleLevelDto.adapt(aclModule));
        }
        return aclModuleListToTree(dtoList);
    }

    /**
     * 权限模块适配方法
     * @param dtoList
     * @return
     */
    public List<AclModuleLevelDto> aclModuleListToTree(List<AclModuleLevelDto> dtoList) {
        if (CollectionUtils.isEmpty(dtoList)) {
            return Lists.newArrayList();
        }
        // level -> [aclmodule1, aclmodule2, ...] Map<String, List<Object>>
        Multimap<String, AclModuleLevelDto> levelAclModuleMap = ArrayListMultimap.create();
        List<AclModuleLevelDto> rootList = Lists.newArrayList();

        for (AclModuleLevelDto dto : dtoList) {
            levelAclModuleMap.put(dto.getLevel(), dto);
            if (LevelUtil.ROOT.equals(dto.getLevel())) {
                rootList.add(dto);
            }
        }
        //排序
        Collections.sort(rootList, new Comparator<AclModuleLevelDto>() {
            @Override
            public int compare(AclModuleLevelDto o1, AclModuleLevelDto o2) {
                return o1.getSeq() - o2.getSeq();
            }
        });
        transformAclModuleTree(rootList, LevelUtil.ROOT, levelAclModuleMap);
        return rootList;
    }

    public void transformAclModuleTree(List<AclModuleLevelDto> dtoList, String level, Multimap<String, AclModuleLevelDto> levelAclModuleMap) {
        for (int i = 0; i < dtoList.size(); i++) {
            AclModuleLevelDto dto = dtoList.get(i);
            String nextLevel = LevelUtil.calculateLevel(level, dto.getId());
            List<AclModuleLevelDto> tempList = (List<AclModuleLevelDto>) levelAclModuleMap.get(nextLevel);
            if (CollectionUtils.isNotEmpty(tempList)) {
                Collections.sort(tempList, new Comparator<AclModuleLevelDto>() {
                    @Override
                    public int compare(AclModuleLevelDto o1, AclModuleLevelDto o2) {
                        return o1.getSeq() - o2.getSeq();
                    }
                });
                dto.setAclModuleList(tempList);
                transformAclModuleTree(tempList, nextLevel, levelAclModuleMap);
            }
        }
    }


//    public void delete(int aclModuleId) {
//        SysAclModule aclModule = sysAclModuleMapper.selectByPrimaryKey(aclModuleId);
//        Preconditions.checkNotNull(aclModule, "待删除的权限模块不存在，无法删除");
//        if(sysAclModuleMapper.countByParentId(aclModule.getId()) > 0) {
//            throw new ParamException("当前模块下面有子模块，无法删除");
//        }
//        if (sysAclMapper.countByAclModuleId(aclModule.getId()) > 0) {
//            throw new ParamException("当前模块下面有用户，无法删除");
//        }
//        sysAclModuleMapper.deleteByPrimaryKey(aclModuleId);
//    }

}
