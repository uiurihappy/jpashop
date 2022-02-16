package jpabook.jpashop.repository;

import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    @PersistenceContext
    EntityManager em;

    public void save(Item item){
        if(item.getId() == null){   //아이템은 처음에 데이터를 저장할 id가 없다.
            em.persist(item);       //그래서 persist를 사용하면 된다.
        } else{                     //merge는 업데이트 비슷하다.
            em.merge(item);         //영속 상태로 되지는 안된다.
        }
    }

    public Item findOne(Long id){
        return em.find(Item.class, id);
    }
    public List<Item> findAll(){
        return em.createQuery("select i from Item i", Item.class)
                .getResultList();
    }


}
