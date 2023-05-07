package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;
    @Override
    public Page page(int page, int pageSize) {
        Page pageinfo = new Page(page, pageSize);
        LambdaQueryWrapper<Category> queryWrapper=new LambdaQueryWrapper<Category>();
        queryWrapper.orderByAsc(Category::getSort);
        return page(pageinfo);
    }

    @Override
    public void remove(String ids) {
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper =new LambdaQueryWrapper<>();
        //添加查询条件,根据分类ID查询
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,Long.parseLong(ids));
        int count1 = dishService.count(dishLambdaQueryWrapper);
        //查询当前分类是否关联了菜品，如果已经失联，则抛出异常
        if(count1>0){
            //已经关联,抛出异常
            throw new CustomException("当前分类关联了菜品,不能删除");
        }
        //查询当前分类是否关联了套餐，如果已经失联，则抛出异常
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper=new LambdaQueryWrapper<>();
        //添加查询条件,根据分类ID查询
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,Long.parseLong(ids));
        int count2 = setmealService.count();
        if(count2>0){
            //已经关联,抛出异常
            throw new CustomException("当前分类关联了套餐,不能删除");
        }
        //正常删除分类
        super.removeById(ids);
    }
}
