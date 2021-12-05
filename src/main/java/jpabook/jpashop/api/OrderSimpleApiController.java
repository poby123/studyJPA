package jpabook.jpashop.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * xToOne 관계 최적화 Order Order -> Member Order -> Delivery
 * 
 */

@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    /**
     * v1. 엔티티 직접 노출
     * - Hibernate5Module 등록. LAZY = null 처리
     * - 엔티티 직접 노출시 양방향 관계 무한루프 문제 -> @JsonIgnore
     * - 쿼리가 총 1 + N + N번 실행되는 문제.
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1(){
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for(Order order : all){
            order.getMember().getName(); // Lazy 로딩 강제 초기화
            order.getDelivery().getAddress(); // Lazy 로딩 강제 초기화
        }

        return all;
    }

    /**
     * v2. 엔티티를 DTO로 변환
     * - 엔티티를 DTO로 변환하는 일반적인 방법.
     * - 쿼리가 총 1 + N + N번 실행되는 문제.
     */
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2(){
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());

        List<SimpleOrderDto> result = orders.stream().
        map(o -> new SimpleOrderDto(o)).
        collect(Collectors.toList());

        return result;
    }

    /**
     * v3. 엔티티를 DTO로 변환 - 페치조인 최적화
     * - fetch join으로 쿼리 1번 호출.
     * - fetch join은 정말 중요하므로 100% 이해해야 함.
     */
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3(){
        List<Order> orders  = orderRepository.findAllWithMemberDelivery();
        
        List<SimpleOrderDto> result = orders.stream().
        map(o -> new SimpleOrderDto(o)).
        collect(Collectors.toList());

        return result;
    }

    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4(){
        return orderSimpleQueryRepository.findOrderDtos();
    }

    @Data
    static class SimpleOrderDto{

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order){
            orderId = order.getId();
            name = order.getMember().getName(); // LAZY 초기화. 미리 fetch 하지 않았을 경우 지연로딩 조회 N번
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); // LAZY 초기화. 미리 fetch 하지 않았을 경우 지연로딩 조회 N번
        }
    }
    
}
