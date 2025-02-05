package com.lcl.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelWriter;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import java.net.URLEncoder;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.io.InputStream;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lcl.common.Result;
import org.springframework.web.multipart.MultipartFile;
import com.lcl.entity.User;
import com.lcl.utils.TokenUtils;

import com.lcl.service.ILeaverecordsService;
import com.lcl.entity.Leaverecords;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 
 * @since 
 */
@RestController
@RequestMapping("/leaverecords")
public class LeaverecordsController {

    @Resource
    private ILeaverecordsService leaverecordsService;

    private final String now = DateUtil.now();

    // 新增或者更新
    @PostMapping
    public Result save(@RequestBody Leaverecords leaverecords) {
        // 检查请假记录的ID是否为空，如果为空，则是一个新的请假记录
        if (leaverecords.getId() == null) {
            leaverecords.setCreateTime(DateUtil.now());
            leaverecords.setName(TokenUtils.getCurrentUser().getUsername());
        }
        leaverecordsService.saveOrUpdate(leaverecords);
        return Result.success();
    }
//根据ID删除
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        leaverecordsService.removeById(id);
        return Result.success();
    }
//批量删除
    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        leaverecordsService.removeByIds(ids);
        return Result.success();
    }
//查询所有
    @GetMapping
    public Result findAll() {
        return Result.success(leaverecordsService.list());
    }
//根据ID查询
    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(leaverecordsService.getById(id));
    }
//    分页查询
    @GetMapping("/page")
    public Result findPage(@RequestParam(defaultValue = "") String name,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        QueryWrapper<Leaverecords> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        if (!"".equals(name)) {
            queryWrapper.like("name", name);
        }
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser.getRole().equals("ROLE_USER")) {
            queryWrapper.eq("name", currentUser.getUsername());
        }
        return Result.success(leaverecordsService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    /**
    * 导出接口
    */
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<Leaverecords> list = leaverecordsService.list();
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("Leaverecords信息表", "UTF-8");
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
        List<Leaverecords> list = reader.readAll(Leaverecords.class);

        leaverecordsService.saveBatch(list);
        return Result.success();
    }

    private User getUser() {
        return TokenUtils.getCurrentUser();
    }

}

