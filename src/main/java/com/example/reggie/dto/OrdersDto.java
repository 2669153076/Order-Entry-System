package com.example.reggie.dto;

import com.example.reggie.domain.OrderDetail;
import com.example.reggie.domain.Orders;
import lombok.Data;

import java.util.List;

@Data
public class OrdersDto extends Orders {
    private String userName;

    private String email;

    private String address;

    private String consignee;

    private List<OrderDetail> orderDetails;
}
