package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

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

    // PUT은 멱등하다.
    @PutMapping("/update/{id}")
    public UpdateMemberResponse updateMemberV2(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateMemberRequest request
    ) {
        memberService.update(id, request.getName());
        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }

    @Data
    static class CreateMemberRequest {
        @NotEmpty
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

    @Data
    static class UpdateMemberRequest {
        @NotEmpty
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        @NotEmpty
        private Long id;
        @NotEmpty
        private String name;

        public UpdateMemberResponse(Long id){
            this.id = id;
        }
    }
}
