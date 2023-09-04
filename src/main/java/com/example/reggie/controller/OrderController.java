package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.common.BaseContext;
import com.example.reggie.common.R;
import com.example.reggie.domain.OrderDetail;
import com.example.reggie.domain.Orders;
import com.example.reggie.domain.ShoppingCart;
import com.example.reggie.domain.User;
import com.example.reggie.dto.OrdersDto;
import com.example.reggie.service.OrderDetailService;
import com.example.reggie.service.OrderService;
import com.example.reggie.service.ShoppingCartService;
import com.example.reggie.service.UserService;
import com.mysql.cj.x.protobuf.MysqlxCrud;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private UserService userService;

    /**
     * 下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    @ResponseBody
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单数据:{}",orders);
        orderService.submit(orders);
        return R.success("下单成功");
    }


    /**
     * 后台查询订单明细
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
    @ResponseBody
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String number,String beginTime,String endTime){
        //分页构造器对象
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        //构造条件查询对象
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();

        //添加查询条件  动态sql  字符串使用StringUtils.isNotEmpty这个方法来判断
        //这里使用了范围查询的动态SQL
        queryWrapper.like(number!=null,Orders::getNumber,number)
                .gt(StringUtils.isNotEmpty(beginTime),Orders::getOrderTime,beginTime)
                .lt(StringUtils.isNotEmpty(endTime),Orders::getOrderTime,endTime);
        queryWrapper.orderByDesc(Orders::getOrderTime);

        orderService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 用户查询订单
     * @param page
     * @param pageSize
     * @return
     */
    @ResponseBody
    @GetMapping("userPage")
    public R<Page> orderDetail(Integer page, Integer pageSize) {
        log.info("page={},pageSize={}",page,pageSize);

        //构造条件分页器
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<OrdersDto>();

        Long currentId = BaseContext.getCurrentId();
        //查询订单数据
        QueryWrapper<Orders> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", currentId);
        Page<Orders> ordersPage = orderService.page(pageInfo, wrapper);

        BeanUtils.copyProperties(pageInfo, ordersDtoPage, "records");

        List<Orders> ordersList = ordersPage.getRecords();

        List<OrdersDto> ordersDtoList = ordersList.stream().map(item -> {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(item, ordersDto);

            Long orderId = item.getId();

            if (orderId != null) {
//                        查询订单明细表
                QueryWrapper<OrderDetail> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("order_id", orderId);
                List<OrderDetail> orderDetailList = orderDetailService.list(queryWrapper);
                ordersDto.setOrderDetails(orderDetailList);
            }

            return ordersDto;

        }).collect(Collectors.toList());

        ordersDtoPage.setRecords(ordersDtoList);

        return R.success(ordersDtoPage);
    }

    /**
     * 订单状态修改(取消，派送，完成)
     * @param map
     * @return
     */
    @ResponseBody
    @PutMapping
    public R<String> editOrderDetail(@RequestBody Map<String,String> map){

        String id = map.get("id");
        Long orderId = Long.parseLong(id);
        Integer status = Integer.parseInt(map.get("status"));

        if(orderId == null || status==null){
            return R.error("传入信息不合法");
        }
        Orders orders = orderService.getById(orderId);
        orders.setStatus(status);
        orderService.updateById(orders);

        return R.success("订单状态修改成功");

    }
}
