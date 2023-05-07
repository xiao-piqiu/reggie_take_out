package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.mapper.EmployeeMapper;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
    @Autowired
    private HttpServletRequest httpServletRequest;
    @Override
    public R login(Employee employee) {
        //获取密码
        String password = employee.getPassword();
        //进行MD5加密
        password=DigestUtils.md5DigestAsHex(password.getBytes());
        //MP数据库查询
        LambdaQueryWrapper<Employee> queryWrapperwrapper = new LambdaQueryWrapper<>();
        queryWrapperwrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = getOne(queryWrapperwrapper);
        //判断是否有此账号
        if(emp==null){
            return R.error("登陆失败");
        }
        //判断密码是否正确
        if(!emp.getPassword().equals(password)){
            return R.error("登陆失败");
        }
        //判断账号是否被禁用
        if(emp.getStatus()==0){
            return R.error("账号已禁用");
        }
        //把ID存进session
        httpServletRequest.getSession().setAttribute("employee",emp.getId());
        //返回结果
        return R.success(emp);
    }

    @Override
    public void logout() {
        //清除session
        httpServletRequest.getSession().removeAttribute("employee");
    }

    @Override
    public boolean save(Employee employee) {
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        return SqlHelper.retBool(this.getBaseMapper().insert(employee));
    }

    @Override
    public Page page(int page, int pageSize, String name) {
        //构造分页构造器
        Page pageinfo = new Page(page, pageSize);
        //构造条件查询器
        LambdaQueryWrapper<Employee> queryWrapper=new LambdaQueryWrapper<Employee>();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        //执行查询
        page(pageinfo,queryWrapper);
        return pageinfo;
    }

    @Override
    public boolean updateById(Employee employee) {
        Long empId = (Long) httpServletRequest.getSession().getAttribute("employee");
        return SqlHelper.retBool(this.getBaseMapper().updateById(employee));
    }

}
