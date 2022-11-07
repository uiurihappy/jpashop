package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {

    @Id @GeneratedValue //시퀀스 값 사용
    @Column(name = "member_id")
    private Long id;

    @NotEmpty
    private String name;

    @Embedded
    private Address address;

    // 반대로 가는 유저의 주문을 ignore를 해야 한다.
    @JsonIgnore
    @OneToMany(mappedBy = "member") //연관 관계의 주인(읽기 전용)
    private List<Order> orders = new ArrayList<>();

}
