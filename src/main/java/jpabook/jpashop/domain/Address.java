package jpabook.jpashop.domain;

import lombok.Getter;

import javax.persistence.Embeddable;

@Embeddable //JPA의 내장 타입(어딘가의 내장이 될 수도 있다.)
@Getter
public class Address {

    private String city;        //도시
    private String street;      //거리
    private String zipcode;     //코드

    //프록시 같은 기술이 없으면 기본 생성자가 필요
    protected Address() {
    }   //함부로 건들면 안된다!

    //생성할 때만 값이 세팅되고, 값이 변경되지 않도록 한다.
    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
