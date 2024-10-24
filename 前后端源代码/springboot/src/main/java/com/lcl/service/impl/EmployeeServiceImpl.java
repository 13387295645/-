package com.lcl.service.impl;

import com.lcl.entity.Employee;
import com.lcl.mapper.EmployeeMapper;
import com.lcl.service.IEmployeeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements IEmployeeService {

}
