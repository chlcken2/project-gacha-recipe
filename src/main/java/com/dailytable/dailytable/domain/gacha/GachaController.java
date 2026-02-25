package com.dailytable.dailytable.domain.gacha;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/gacha")
public class GachaController {
    @GetMapping("/home")
    public String getGachaHome() {
        return "recipe-create";
    }
}
