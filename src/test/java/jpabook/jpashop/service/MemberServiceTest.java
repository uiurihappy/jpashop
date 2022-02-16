package jpabook.jpashop.service;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)        // junit 실행할 때 스프링이랑 엮어서 실행
@SpringBootTest                     // 스프링를 뭔가 띄운 상태로 테스트 작업할 때 넣어야 함
@Transactional                      // 테스트를 실행하면 다 롤백을 해버린다.
public class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired
    EntityManager em;

    @Test
    public void 회원가입() throws Exception{
        //given
        Member member = new Member();
        member.setName("kim");

        //when
        Long saveId = memberService.join(member);

        //then
        em.flush(); //영속성 컨텍스트가 멤버 객체 들어가는 걸 확인할 수 있다.
        assertEquals(member, memberRepository.findOne(saveId));

        // generatevalue 전략에서는 persist로 save를 했기에 insert문이 안나온다.

    }
    @Test(expected = IllegalStateException.class)   //굳이 try-catch문을 작성안해도 된다.
    public void 중복_회원_예외() throws Exception{
        //given
        Member member1 = new Member();
        member1.setName("kim");

        Member member2 = new Member();
        member2.setName("kim");

        //when
        memberService.join(member1);
        // try {
        memberService.join(member2);    //예외가 발생한다.
        // } catch (IllegalStateException e){
        //    return;
        //}

        //then
        fail("예외가 발생해야 한다.");


    }


}