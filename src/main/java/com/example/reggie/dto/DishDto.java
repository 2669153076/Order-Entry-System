package com.example.reggie.dto;

import com.example.reggie.domain.Dish;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {

    private String categoryName;

    private Integer copies;
}
