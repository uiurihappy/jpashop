package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)     // 데이터 변경을 위해 필요, 읽기 전용
//@AllArgsConstructor               // 롬복의 방법으로 생성자 주입 시 사용된다.
@RequiredArgsConstructor            // final이 가지고 있는 필드만 가지고 생성자를 만들어준다.
public class MemberService {

    // 요즘 사용하는 방식으로 constructor injection을 사용한다.
    private final MemberRepository memberRepository;
    // 장점: 테스트케이스를 작성할 때, memberService로 예시를 들면 무언가 놓치지 않고 해결가능
    // @Autowired // 최신 스프링 기법으로는 @Autowired를 사용하지 않는다.
    // 왜냐면 스프링이 자동으로 주입을 시켜준다. 대신 final을 작성해야한다.
//    public MemberService(MemberRepository memberRepository) {
//        this.memberRepository = memberRepository;
//    }

    // @Autowired  //setter injection
    // 장점: 중간에 테스트 코드를 작성 중 직접 주입이 가능
    // 단점: runtime 중에 누군가가 코드를 바꿀 수가 있다.
//    public void setMemberRepository(MemberRepository memberRepository) {
//        this.memberRepository = memberRepository;
//    }

    //회원 가입
    @Transactional  //데이터 변경을 위해 필요
    public Long join(Member member){
        // 회원 중복 체크 후 저장
        validateDuplicateMember(member);    //중복 회원 검증
        memberRepository.save(member);
        return member.getId();
    }

    private void validateDuplicateMember(Member member) {
        //Exception
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if(!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }

    }

    //회원 전체 조회
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }
    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }

}
