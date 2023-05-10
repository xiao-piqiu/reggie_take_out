package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Orders;

import java.util.Date;

public interface OrderService extends IService<Orders> {
    void submit(Orders orders);
    Page<Orders> page(int page, int pageSize, Integer number, Date beginTime, Date endTime);

}
