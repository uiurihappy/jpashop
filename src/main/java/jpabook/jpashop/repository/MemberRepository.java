package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import java.util.List;

@Repository //컴포넌트 스캔에서 스프링으로 자동 등록
@RequiredArgsConstructor
public class MemberRepository {
    //@Autowired
    //@PersistenceContext           //표준 annotation //JPA의 entityManager를
    private final EntityManager em; //스프링이 entityManager를 만들어서 주입을 시켜준다.

    // @PersistenceUnit    // entityManagerFactory를 직접 주입받고 싶다면
    // private EntityManagerFactory emf;

    public void save(Member member){
        em.persist(member);
    }

    //조회
    public Member findOne(Long id){
        return em.find(Member.class, id);   //타입, PK
    }

    public List<Member> findAll(){
        //JPQL, 반환 타입
        return em.createQuery("select m from Member m", Member.class)
            .getResultList();
    }

    public List<Member> findByName(String name){
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)    //파라미터 바인딩
                .getResultList();
    }


}
