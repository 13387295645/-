package com.lcl.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelWriter;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import java.net.URLEncoder;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lcl.entity.Employee;
import com.lcl.service.IEmployeeService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.io.InputStream;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lcl.common.Result;
import org.springframework.web.multipart.MultipartFile;
import com.lcl.entity.User;
import com.lcl.utils.TokenUtils;

import com.lcl.service.ISalaryService;
import com.lcl.entity.Salary;

import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/salary")
public class SalaryController {

    @Resource
    private ISalaryService salaryService;

    @Resource
    private IEmployeeService employeeService;

    private final String now = DateUtil.now();

    // 新增或者更新
    @PostMapping
    public Result save(@RequestBody Salary salary) {

        //总工资 = 基本工资+奖金-扣款
        int sumSalary = salary.getBasicSalary();
        // 检查并设置 bonus 和 deduction 为0，如果它们为null
        if (salary.getBonus() == null) {
            salary.setBonus(0);
        }
        if (salary.getDeduction() == null) {
            salary.setDeduction(0);
        }
        sumSalary += salary.getBonus();
        sumSalary -= salary.getDeduction();

        salary.setTotalSalary(sumSalary);

        salaryService.saveOrUpdate(salary);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        salaryService.removeById(id);
        return Result.success();
    }

    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        salaryService.removeByIds(ids);
        return Result.success();
    }

    @GetMapping
    public Result findAll() {
        return Result.success(salaryService.list());
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(salaryService.getById(id));
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam(defaultValue = "") String name,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        QueryWrapper<Salary> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        if (!"".equals(name)) {
            queryWrapper.like("employee", name);
        }
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser.getRole().equals("ROLE_USER")) {
            //employee当前页面表中的字段
            queryWrapper.eq("employee", currentUser.getNickname());
        }

        List<Employee> employeeList = employeeService.list();
        Page<Salary> page = salaryService.page(new Page<>(pageNum, pageSize), queryWrapper);
        for (Salary record : page.getRecords()) {
            employeeList.stream()
                    .filter(employee -> employee.getName().equals(record.getEmployee()))
                    .findFirst()
                    .ifPresent(employee -> {
                        record.setEmployeeId(employee.getId());
                        // 在这里使用 salaryService 进行更新操作，将更改保存到数据库
                        salaryService.updateById(record);
                    });
        }


        return Result.success(page);
    }

    /**
    * 导出接口
    */
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<Salary> list = salaryService.list();
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("Salary信息表", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        out.close();
        writer.close();

        }

    /**
     * excel 导入
     * @param file
     * @throws Exception
     */
    @PostMapping("/import")
    public Result imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
        List<Salary> list = reader.readAll(Salary.class);

        salaryService.saveBatch(list);
        return Result.success();
    }

    private User getUser() {
        return TokenUtils.getCurrentUser();
    }

}

