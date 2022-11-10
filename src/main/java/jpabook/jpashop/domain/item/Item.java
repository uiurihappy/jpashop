package jpabook.jpashop.domain.item;

import jpabook.jpashop.domain.Category;
import jpabook.jpashop.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // 싱글테이블 상속 전략
@DiscriminatorColumn(name = "dtype")    //book이면 어떻게 할것이냐
@Getter @Setter
public abstract class Item {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    private String name;        //상품 이름
    private int price;          //상품 가격
    private int stockQuantity;  //상품 재고 수량

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

    // -- 비즈니스 로직 -- (응집력 강화)//

    //재고 수량을 증가하는 로직
    /*
        stock 증가
     */
    public void addStock(int quantity){
        this.stockQuantity += quantity;
    }
    /*
        stock 감소
     */
    public void removeStock(int quantity){
        int restock = this.stockQuantity - quantity;            //재고 감소
        if(restock < 0){                                        //재고가 0보다 적을 때 예외
            throw new NotEnoughStockException("need more stock");
        }
        this.stockQuantity = restock;
    }


}
