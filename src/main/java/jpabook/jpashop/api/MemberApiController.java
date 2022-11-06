package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;

// @Controller , @ResponseBody 합친 것
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/members")
public class MemberApiController {

    // final class
    private final MemberService memberService;

    // 기존 안 좋은 예시 v1
    @GetMapping("/get")
    public List<Member> memberV1(){
        // 1. 엔티티가 직접 외부에 노출되서 문제
        // 2. 엔티티가 변경되면 API 스펙이 변경되므로 DTO를 사용하는 것을 권장
        return memberService.findMembers();
    }

    // 회원 조회 API v2
    @GetMapping("/getV2")
    public MemberResult<List<MemberDTO>> memberV2() {
        List<Member> findMembers = memberService.findMembers();
        List<MemberDTO> members = findMembers.stream()
                .map(item -> new MemberDTO(item.getName()))
                .collect(Collectors.toList());
        // 회원 수
        Integer count = members.size();
        return new MemberResult<>(count, members);
    }

    @Data
    @AllArgsConstructor
    static class MemberResult<T> {
        private Integer count;
        private T data;
    }

    // 회원 조회 DTO
    @Data
    @AllArgsConstructor
    static class MemberDTO {
        private String name;
    }


    // 회원 등록 API v2
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
