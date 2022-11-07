package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jpabook.jpashop.domain.item.Item;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

import static javax.persistence.FetchType.*;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "item_id")
    private Item item;          //주문 상품

    // 반대로 가는 주문의 주문 상품을 ignore를 해야 한다.
    @JsonIgnore
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "order_id")
    private Order order;        //주문

    private int orderPrice;     //주문 가격
    private int count;          //주문 수량

    // 다른 스타일의 생성을 막기 위해
    // @NoArgsConstructor(access = AccessLevel.PROTECTED)
    // protected OrderItem() { } 둘중 하나 사용

    //==생성 메서드==//
    public static OrderItem createOrderItem(Item item, int orderPrice, int count){
        //주문 가격과 수량이 바뀔 수도 있으니 새로 선언
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);

        //기본적으로 재고를 줄여줘야한다. 즉, 넘어온 만큼 재고를 줄여줘야 한다.
        item.removeStock(count);
        return orderItem;
    }

    //==비즈니스 로직==//
    public void cancel() {
        getItem().addStock(count);  //재고수량을 복구해줘야한다.

    }

    //==조회 로직==//
    /*
        주문상품 전체 가격 조회
     */
    public int getTotalPrice() {
        return getOrderPrice() * getCount();
    }
}
