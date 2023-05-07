package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        dishService.saveWithFlavor(dishDto);
        return R.success("新增成功");
    }
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        Page<Dish> pageinfo=new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage=new Page<>();
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.like(name!=null,Dish::getName,name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        dishService.page(pageinfo,queryWrapper);
        //拷贝除records之外的属性recorods:数据库中查询出来的数据
        BeanUtils.copyProperties(pageinfo,dishDtoPage,"records");
        //获取pageinfo中的records数组
        List<Dish> records = pageinfo.getRecords();
        List<DishDto> list=records.stream().map((item)->{
            DishDto dishDto=new DishDto();
            //把pageinfo中的数据拷贝进要返回给前端的dishdto中
            BeanUtils.copyProperties(item,dishDto);
            //取出item中的categoryid:菜品分类id
            Long categoryId = item.getCategoryId();
            //用菜品分类ID:categoryId去数据库中查询此菜品分类的数据
            Category category = categoryService.getById(categoryId);
            //提取菜品分类的名称
            String categoryName = category.getName();
            //把菜品分类名称设置给dishDto
            dishDto.setCategoryName(categoryName);
            return dishDto;
        }).collect(Collectors.toList()) ;
        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        dishService.updateWithFlavor(dishDto);
        return R.success("新增成功");
    }
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus,1);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);
        List<DishDto> dishDtos=list.stream().map((item)->{
            DishDto dishDto=new DishDto();
            //把pageinfo中的数据拷贝进要返回给前端的dishdto中
            BeanUtils.copyProperties(item,dishDto);
            //取出item中的categoryid:菜品分类id
            Long categoryId = item.getCategoryId();
            //用菜品分类ID:categoryId去数据库中查询此菜品分类的数据
            Category category = categoryService.getById(categoryId);
            //提取菜品分类的名称
            String categoryName = category.getName();
            //把菜品分类名称设置给dishDto
            dishDto.setCategoryName(categoryName);
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList()) ;
        return R.success(dishDtos);
    }
}
