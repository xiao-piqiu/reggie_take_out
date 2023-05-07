package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;

public interface EmployeeService extends IService<Employee> {
    R login(Employee employee);

    void logout();

    Page page(int page, int pageSize, String name);

    boolean updateById(Employee employee) ;

}
