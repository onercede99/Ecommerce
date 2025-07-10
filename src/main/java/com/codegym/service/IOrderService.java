package com.codegym.service;

import java.math.BigDecimal;

public interface IOrderService {
    long count();
    BigDecimal calculateTotalRevenue();

}
