package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

// @Controller , @ResponseBody 합친 것
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/members")
public class MemberApiController {

    // final class
    private final MemberService memberService;

    // 회원 등록 API
    @PostMapping("/set")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member){
        // Vaild javax validation이 자동으로 이뤄진다.
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    /**
     * 회원 등록 API v2
     * @param request
     * @return member id
     */
    @PostMapping("/setV2")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request){
        Member member = new Member();

        member.setName(request.getName());
//        System.out.println(member);
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @Data
    static class CreateMemberRequest {
        private String name;
    }

    @Data
    static class CreateMemberResponse{
        private Long id;

        // 생성자
        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }
}
