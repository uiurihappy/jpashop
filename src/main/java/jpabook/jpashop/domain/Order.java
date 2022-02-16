package jpabook.jpashop.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.*;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    // 멤버랑 관계 setting
    //ManyToOne은 전부 다 코드를 찾아서 Lazy로 바꿔야 한다.
    @ManyToOne(fetch = LAZY) //다대일   //order를 조회할 때 member를 조인해서 같이 가진다.
    @JoinColumn(name = "member_id") //매핑을 member_id로 잡는다.
    private Member member;

    //cascade All이란 order를 persist하면 들어와있는 컬렉션 OrderItem도 다 persist를 강제로 날려준다.
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)   //orderItems에 데이터만 넣어두고
    // order를 저장하면 같이 저장된다.
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(fetch = LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status; //주문 상태를 보여준다.(ORDER, CANCEL)

    //==연관관계 메서드==//
    // 양방향 생성을 위해서 세팅
    public void setMember(Member member) {
        this.member = member;
        member.getOrders().add(this);   //반대로 양방향 연관관계 설계
    }
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);       //양방향 연관관계
    }
    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.setOrder(this);        //양방향 연관관계
    }

    //==생성 메서드==//
    //생성을 할때부터 바로 createOrder를 호출해야 한다.
    //
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems){
        //...문법은 여러 개를 넘길 수 있다.
        // Order가 연관관계를 쫙 걸면서 세팅이 되고, 상태랑 주문시간까지 세팅
        Order order = new Order();
        order.setMember(member);
        order.setDelivery(delivery);

        for(OrderItem orderItem : orderItems){
            order.addOrderItem(orderItem);
        }

        order.setStatus(OrderStatus.ORDER);         //처음 상태로 Order를 강제로 한다.
        order.setOrderDate(LocalDateTime.now());    //현재 시간으로 잡는다.
        return order;

    }

    //==비즈니스 로직==//
    /*
        주문취소
     */
    public void cancel(){
        if(delivery.getStatus() == DeliveryStatus.COMP){   //배송이 시작되면 취소가 안되도록
            throw new IllegalStateException("이미 배송이 시작된 상품은 취소가 불가능 합니다.");
        }
        this.setStatus(OrderStatus.CANCLE);     //상태를 바꿔주고
        for(OrderItem orderItem : orderItems){
            orderItem.cancel();                 //루프를 돌면서 cancel를 하면 원복시켜준다.
        }
    }

    //==조회 로직==//
    /*
        전체 주문 가격 조회
     */
    public int getTotalPrice(){
        int totalPrice = 0;
        for(OrderItem orderItem : orderItems){
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;
    }


}
