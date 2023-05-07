package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;


@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private HttpServletRequest request;
    @PostMapping("/login")
    public R login(@RequestBody Employee employee){
        return employeeService.login(employee);
    }
    @PostMapping("/logout")
    public R logout(){
        employeeService.logout();
        return R.success("退出成功");
    }
    @PostMapping
    public R save(@RequestBody Employee employee){
        employeeService.save(employee);
        return R.success("添加成功");
    }
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        return R.success(employeeService.page(page,pageSize,name));
    }
    @PutMapping
    public R<String> update(@RequestBody Employee employee){
        employeeService.updateById(employee);
        return R.success("修改成功");
    }
    @GetMapping("/{id}")
    public R select(@PathVariable("id") String id){
        return R.success(employeeService.getById(id));
    }
}
