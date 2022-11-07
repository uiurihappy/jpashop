package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

import static javax.persistence.FetchType.*;

@Entity
@Getter @Setter
public class Delivery {

    @Id @GeneratedValue
    @Column(name = "delivery_id")
    private Long id;

    // 반대로 가는 주문의 배달을 ignore를 해야한다.
    @JsonIgnore
    @OneToOne(mappedBy = "delivery", fetch = LAZY)    //delivery를 연관관계의 주인으로 선정
    private Order order;

    @Embedded   //내장 타입
    private Address address;

    //enum 타입을 조심해야 하는 이유
    //ORDINAL이 아닌 STRING으로 넣어야 하는 이유: DB조회할때 중간중간 삽입되도 순서에 상관이 없다.
    @Enumerated(EnumType.STRING)   //얘를 넣어줘야 한다.
    private DeliveryStatus status;  //READY, COMP(배송 준비와 배송)


}
