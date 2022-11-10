package jpabook.jpashop.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.QMember;
import jpabook.jpashop.domain.QOrder;
import jpabook.jpashop.repository.dtos.orderDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

import static jpabook.jpashop.domain.QMember.member;
import static jpabook.jpashop.domain.QOrder.order;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order){
        em.persist(order);
    }
    public Order findOne(Long id){
        return em.find(Order.class, id);
    }

    //JPQL로 처리

    public List<Order> findAllByString(OrderSearch orderSearch){
        //language=JPAQL
        String jpql = "select o From Order o join o.member m";
        boolean isFirstCondition = true;
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }
        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000); //최대 1000건
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }
        return query.getResultList();
    }


    /*
        JPA Criteria
        단점: 유지보수하기 힘들다.
     */
    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);

        Join<Object, Object> m = o.join("member", JoinType.INNER);
        List<Predicate> criteria = new ArrayList<>();

        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"),
                    orderSearch.getOrderStatus());
            criteria.add(status);
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name =
                    cb.like(m.<String>get("name"), "%" +
                            orderSearch.getMemberName() + "%");
            criteria.add(name);
        }

        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000); //최대 1000건
        return query.getResultList();

    }

    public List<Order> findAll(OrderSearch orderSearch){
        JPAQueryFactory query = new JPAQueryFactory(em);

        return query
                .select(order)
                .from(order)
                .join(order.member, member)
                // 동적 쿼리
                .where(statusEq(orderSearch.getOrderStatus()), nameLike(orderSearch.getMemberName()))
                .limit(1000)
                .fetch();
    }

    private BooleanExpression nameLike(String memberName) {
        if (!StringUtils.hasText(memberName) ) return null;
        return member.name.like(memberName);
    }

    private BooleanExpression statusEq(OrderStatus statusCode) {
        if (statusCode == null) return null;

        return order.status.eq(statusCode);
    }
    // fetch join
    // member랑 delivery를 join하여 한번에 가져온다.
    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class
        ).getResultList();
    }

    public List<Order> findAllWithMemberDeliveryV2(int offset, int limit) {
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }


    /**
     * fetch join으로 한번에 다 들고옴
     * order -> member, order -> delivery는 이미 order에서 조회된 상태라 지연로딩이 되지 않고 전혀 무관함
     * 정말 자주 사용하고 권장이 아니라 relation 엮을 때는 거의 필수라 보는 경우
     */

    public List<orderDTO> findOrderDtos() {
        return em.createQuery(
                "select new jpabook.jpashop.repository.dtos.orderDTO(o.id, m.name, o.orderDate, o.status, d.address) " +
                        "from Order o" +
                        " join o.member m" +
                        " join o.delivery d", orderDTO.class
        ).getResultList();
    }

    // 문제1: order가 두 번씩 찍히는 현상이 발생

    public List<Order> findAllWithItem(OrderSearch orderSearch) {
        // distinct를 통해 order의 ref를 중복 제거하여 추출한다.
        // 그러나 db 쿼리를 뽑을 때와는 다르다.
        // jpa에서 자체적으로 distinct가 있으면 만약 pk id가 같다? 그럼 자체적으로 중복을 제거해준다.
        return em.createQuery(
                // 동작과정
                // 순서는 db에 distinct를 먼저 날려 쿼리를 쏘고,
                // entity가 중복인 경우 제거를 하여 collection을 return 해준다.
                // 여기서 order의 ToOne 관계 테이블은 member와 delivery이다.
                "select distinct o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d" +
                        " join fetch o.orderItems oi" +
                        " join fetch oi.item i", Order.class)
                .getResultList();
    }
    /*
         query에 있는 o가 매핑될 수가 없다.
         엔티티나 객체는 반환 받을 수 있지만 dto는 반환받을 수 없다.
         할려면 new 연산자를 사용해야한다.
         얘는 재사용성이 떨어지고, 한 API에 너무 의존적이라 스펙이 바뀌면 당장 뜯어야 하는 구조이고 코드 수정이 많아진다.
         그리고 코드가 더럽다...
     */


}
