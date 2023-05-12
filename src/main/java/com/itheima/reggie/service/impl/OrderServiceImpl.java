package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.mapper.OrderMapper;
import com.itheima.reggie.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private UserService userService;
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Override
    @Transactional
    public void submit(Orders orders) {
        Long userId = BaseContext.getCurrentId();
        //获取购物车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCarts= shoppingCartService.list(queryWrapper);
        if (shoppingCarts==null ||shoppingCarts.size()==0){
            throw new CustomException("购物车为空,不能下单");
        }
        //查询用户信息
        User user = userService.getById(userId);
        //查询地址信息
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if (addressBook==null){
            throw new CustomException("地址信息有误,不能下单");
        }
        long orderId = IdWorker.getId();
        AtomicInteger amount=new AtomicInteger(0);
        List<OrderDetail> orderDetailList=shoppingCarts.stream().map((item)->{
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());
        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(userId);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
        this.save(orders);
        orderDetailService.saveBatch(orderDetailList);
        shoppingCartService.remove(queryWrapper);
    }

    @Override
    public Page<Orders> page(int page, int pageSize, Integer number, Date beginTime, Date endTime) {
        Page<Orders> pageinfo=new Page<>(page,pageSize);
        LambdaQueryWrapper<Orders> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.like(number!=null,Orders::getNumber,number);
        queryWrapper.between(beginTime!=null && endTime!=null,Orders::getOrderTime,beginTime,endTime);
        return super.page(pageinfo, queryWrapper);
    }

    @Override
    public Page<OrdersDto> userPage(int page, int pageSize) {
        Page<Orders> pageinfo=new Page<>(page,pageSize);
        Page<OrdersDto> ordersDtoPage=new Page<>();
        LambdaQueryWrapper<Orders> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId,BaseContext.getCurrentId());
        queryWrapper.orderByDesc(Orders::getOrderTime);
        this.page(pageinfo,queryWrapper);
        BeanUtils.copyProperties(pageinfo,ordersDtoPage,"records");
        List<Orders> records = pageinfo.getRecords();
        List<OrdersDto> collect = records.stream().map((item) -> {
            OrdersDto ordersDto=new OrdersDto();
            BeanUtils.copyProperties(item, ordersDto);
            LambdaQueryWrapper<OrderDetail> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(OrderDetail::getOrderId, item.getId());
            List<OrderDetail> orderDetailList = orderDetailService.list(lambdaQueryWrapper);
            ordersDto.setOrderDetails(orderDetailList);
            return ordersDto;

        }).collect(Collectors.toList());
        return ordersDtoPage.setRecords(collect);
    }

}
