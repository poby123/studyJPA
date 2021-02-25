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

    @Transactional
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    /*
    * Item 을 변경감지를 통해 수정하는 함수. 실무에서는 merge 보다 이 함수처럼 변경감지를 통해 수정하는 것이 권장된다.
    * Transactional 어노테이션이 붙어있야 한다.
    * */
    @Transactional
    // 파라미터가 많아지면 DTO 를 이용하면 된다.
    public void updateItem(Long itemId, String name, int price, int stockQuantity){
        // repository 에서 find 를 통해 가져온 객체는 영속상태의 객체이므로, 객체를 수정하고 별도로 save 나 merge 를 하지 않아도 된다.
        Item findItem = itemRepository.findOne(itemId);
        findItem.setName(name);
        findItem.setPrice(price);
        findItem.setStockQuantity(stockQuantity);

        // 생략
//        findItem.setAuthor(param.getAuthor());
//        findItem.setIsbn(param.getIsbn());
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    public Item findOne(Long itemId) {
        return itemRepository.findOne(itemId);
    }
}
