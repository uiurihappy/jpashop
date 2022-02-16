package jpabook.jpashop.domain.item;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("A")   //저장될 때 구분을 해주기 위해
@Getter @Setter
public class Album extends Item{

    private String artist;
    private String etc;

}
