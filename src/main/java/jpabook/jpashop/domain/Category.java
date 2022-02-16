package jpabook.jpashop.domain;

import jpabook.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.*;

@Entity
@Getter @Setter
public class Category {

    @Id @GeneratedValue
    @Column(name = "category_id")
    private Long id;

    private String name;

    @ManyToMany
    @JoinTable(name = "category_item",
            joinColumns = @JoinColumn(name = "category_id"), //중간 테이블에 있는 카테고리 아이디
            inverseJoinColumns = @JoinColumn(name = "item_id") //아이템 쪽으로 들어감
    )  //중간 테이블을 매핑해줘야 한다.
    private List<Item> items = new ArrayList<>();

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;    //계층구조 확인을 위해 선언

    @OneToMany(mappedBy = "parent")
    private List<Category> child = new ArrayList<>();

    //연관관계 메서드//
    //부모 컬렉션, 자식에서도 this로 관계를 넣어준다.
    public void addChildCategory(Category child){
        this.child.add(child);
        child.setParent(this);
    }

}
