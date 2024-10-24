package com.lcl.service.impl;

import com.lcl.entity.Salary;
import com.lcl.mapper.SalaryMapper;
import com.lcl.service.ISalaryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


@Service
public class SalaryServiceImpl extends ServiceImpl<SalaryMapper, Salary> implements ISalaryService {

}
