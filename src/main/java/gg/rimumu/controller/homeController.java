package gg.rimumu.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class homeController extends BaseController {

    @GetMapping("/")
    public String home(){
        return "home";
    }

}
