package jpabook.jpashop.controller;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
public class HomeController {

    @RequestMapping("/")         //현재 디렉토리
    public String home(){
        log.info("home controller");
        return "home";              //home.html을 찾아서 타임리프 파일을 찾아가게 된다.
    }
}
