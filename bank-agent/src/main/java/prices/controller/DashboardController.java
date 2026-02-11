package prices.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/sber")
    public String sberBank() {
        return "sber";
    }

    @GetMapping("/health")
    @ResponseBody
    public String health() {
        return "OK";
    }
}
