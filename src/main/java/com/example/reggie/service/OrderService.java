package com.example.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.reggie.domain.Orders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
@Resource
public interface OrderService extends IService<Orders> {

    /**
     * 用户下单
     * @param orders
     */

    public void submit(Orders orders);
}
