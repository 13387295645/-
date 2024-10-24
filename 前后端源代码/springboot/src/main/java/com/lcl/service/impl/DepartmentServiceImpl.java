package com.lcl.service.impl;

import com.lcl.entity.Department;
import com.lcl.mapper.DepartmentMapper;
import com.lcl.service.IDepartmentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements IDepartmentService {

}
