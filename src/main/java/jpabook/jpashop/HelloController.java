package jpabook.jpashop;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {

    @GetMapping("hello")
    public String hello(Model model){
        //서버 사이드에 렌더링한다.
        model.addAttribute("data", "hello!!!");
        return "hello";     //화면 이름(자동으로 html확장자를 가리켜준다.)
    }


}
