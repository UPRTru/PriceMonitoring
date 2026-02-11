package com.precious.general.controller;

import com.precious.general.service.ServiceCheckPrice;
import com.precious.shared.dto.CheckPrice;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class PriceController {

    private final ServiceCheckPrice serviceCheckPrice;

    public PriceController(ServiceCheckPrice serviceCheckPrice) {
        this.serviceCheckPrice = serviceCheckPrice;
    }

    @PostMapping("/check/{email}")
    public void check(@RequestBody CheckPrice checkPrice, @PathVariable String email) throws Exception {
        serviceCheckPrice.checkPrice(email, checkPrice);
    }
}