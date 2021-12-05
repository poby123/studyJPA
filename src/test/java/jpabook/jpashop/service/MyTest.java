package jpabook.jpashop.service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
@Transactional
public class MyTest {
    @Autowired
    EntityManager em;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    MemberService memberService;

    @Test
    public void 멤버_책_생성(){
        Member member1 = createMember("홍길동");
        log.info("before order ================= ");
        for(Order order : member1.getOrders()){
            log.info(order.getId() + " ");         
        }

        Book book1 = createBook("JPA", 30000, 2000);
        Book book2 = createBook("Spring boot", 40000, 200);

        Long order1 = orderService.order(member1.getId(), book1.getId(), 1);
        Long order2 = orderService.order(member1.getId(), book2.getId(), 2);

        log.info("after order ================= ");
        member1 = memberService.findOne(member1.getId());
        for(Order order : member1.getOrders()){
            log.info(order.getId() + " ");         
        }

        Order result = orderRepository.findOne(order1);
        result.getMember();
        log.info(result.getMember().getName());
    }

    private Book createBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        
        return book;
    }

    private Member createMember(String name){
        Member ret = new Member();
        ret.setName(name);
        em.persist(ret);

        return ret;
    }


}
