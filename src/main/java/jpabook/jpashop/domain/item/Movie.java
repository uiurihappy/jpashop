package jpabook.jpashop.domain.item;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("M")   //저장될 때 구분을 해주기 위해
@Getter
@Setter
public class Movie extends Item{

    private String director;
    private String actor;

}
