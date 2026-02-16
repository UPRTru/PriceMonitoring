package com.precious.general.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class DashboardController {

    @Value("${bank.agent.url}")
    private String bankAgentUrl;

    @Value("${user.service.url}")
    private String userServiceUrl;

    @Value("${bank.agent.out-url}")
    private String bankAgentOutUrl;

    @Value("${user.service.out-url}")
    private String userServiceOutUrl;

    @GetMapping("/")
    public String dashboard() {
        Map<String, Boolean> status = moduleStatus();
        if (status.get("bankAgent") && status.get("userService")) {
            return "redirect:" + userServiceOutUrl + "?all=true";
        } else if (status.get("bankAgent")) {
            return "redirect:" + bankAgentOutUrl;
        } else if (status.get("userService")) {
            return "redirect:" + userServiceOutUrl;
        }
        return "unavailable";
    }

    @GetMapping("/api/bank_module")
    public String getBankModule() {
        if (isReachable(bankAgentUrl + "/health")) {
            return "redirect:" + bankAgentOutUrl;
        } else {
            return "unavailable";
        }
    }

    @GetMapping("/api/modules/status")
    @ResponseBody
    public Map<String, Boolean> moduleStatus() {
        Map<String, Boolean> status = new HashMap<>();
        status.put("bankAgent", isReachable(bankAgentUrl + "/health"));
        status.put("userService", isReachable(userServiceUrl + "/health"));
        return status;
    }

    private boolean isReachable(String url) {
        try {
            java.net.URL u = new java.net.URL(url);
            java.net.HttpURLConnection h = (java.net.HttpURLConnection) u.openConnection();
            h.setRequestMethod("GET");
            h.setConnectTimeout(1000);
            h.setReadTimeout(1000);
            return h.getResponseCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
