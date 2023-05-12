package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        orderService.submit(orders);
        return R.success("下单成功");
    }
    @GetMapping("/page")
    public R<Page<Orders>> page(int page, int pageSize, Integer number, Date beginTime,Date endTime){
        Page<Orders> ordersPage = orderService.page(page, pageSize, number, beginTime, endTime);
        return R.success(ordersPage);
    }
    @PutMapping
    public R<String> updateStats(@RequestBody Orders orders){
        orderService.updateById(orders);
        return R.success("派送成功");
    }
    @GetMapping("/userPage")
    public R<Page<OrdersDto>> userPage(int page, int pageSize){
        return R.success(orderService.userPage(page,pageSize));
    }
}
