package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional      //데이터를 수정, 변경, 저장이 필요
    public void saveItem(Item item){
        itemRepository.save(item);
    }

    //변경 감지 기능
    @Transactional      //트랜잭션이 커밋되면 jpa는 플러쉬를 날린다.
    //플러쉬를 날린다는것은 영속성 컨텍스트에 있는 엔티티중에 변경된 것을 다 찾는다.
    //데이터를 관리할때 UpdateItemDTO를 파라미터로 이용할 수도 있음
    public void updateItem(Long itemId, String name, int price, int stockQuantity){
        // find해서 item을 찾는다.
        // id를 기반으로 영속상태의 entity를 찾는다.
        // id가 있으면 트랜잭션 안에서 엔티티를 조회해야 영속 상태 조회가 가능하고
        // 거기에 값을 변경해야 변경감지가 일어날 수 있다.
        Item findItem = itemRepository.findOne(itemId);
        findItem.setPrice(price);
        findItem.setName(name);
        findItem.setStockQuantity(stockQuantity);
        //itemRepository를 호출할 필요가 없음
    }

    //전체 조회
    public List<Item> findItems(){
        return itemRepository.findAll();
    }

    //한 개 조회
    public Item findOne(Long itemId){
        return itemRepository.findOne(itemId);
    }

}
