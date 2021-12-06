package jpabook.jpashop.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OrderApiController {
    
    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    /**
     * v1. 엔티티 직접 노출
     * - Hibernate5Module 등록. Lazy = null 처리
     * - 엔티티를 직접 노출하므로 좋은 방법이 아니다.
     * - N+1문제 발생.
     * 
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1(){
        List<Order> all = orderRepository.findAllByString(new OrderSearch());

        for(Order order : all){
            order.getMember().getName(); // Lazy 강제 초기화
            order.getDelivery().getAddress(); // Lazy 강제 초기화
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName()); // Lazy 강제 초기화
        }

        return all;
    }


    /**
     * v2. 엔티티를 DTO로 변환
     * 지연로딩으로 너무 많은 SQL이 실행된다↓
     * 
     * order : 1번
     * member, address : N번(조회된 order 수만큼)
     * orderItem : N번(조회된 order 수만큼)
     * item : M번 (조회된 orderItem 수만큼)
     */
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2(){
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> result = orders.stream().map(o -> new OrderDto(o)).collect(Collectors.toList());

        return result;
    }

    
    /**
     * v3. 엔티티를 DTO로 변환 + 페치조인 최적화
     * 
     * fetch join으로 SQL 쿼리가 한 번만 날라간다!!
     * 페이징이 불가능하다.
     * => 되는 것처럼 보이지만, 모든 데이터를 읽어와서 메모리에서 페이징을 하기 떄문에 매우 위험하다.
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3(){
        List<Order> orders = orderRepository.findAllWithItem();
        List<OrderDto> result = orders.stream().map(o -> new OrderDto(o)).collect(Collectors.toList());
        return result;
    }


    /**
     * v3.1. 엔티티를 DTO로 변환 + 페이징과 한계 돌파
     * - 1. XtoOne 관계는 모두 페치조인한다. ToOne 관계는 row수를 증가시키지 않기 때문에 페이징하는데 상관이 없다.
     * - 2. 컬렉션은 지연로딩으로 조회한다.
     *   - 지연로딩 성능 최적화를 위해서 hibernate.default_batch_fetch_size 또는 @BatchSize를 적용한다.
     *   - 위 옵션을 사용하면, 컬렉션이나 프록시 객체를 한꺼번에 설정한 size만큼 IN 쿼리로 조회한다.
     * 
     * 
     * 페이징이 가능하며, 쿼리 호출수가 1 + N에서 1 + 1로 최적화된다.
     * v3와 비교해보면, 중복데이터가 없고, 쿼리 호출수가 약간 증가하지만 DB 데이터 전송량은 감소한다.
     * 
     */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(@RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);
        List<OrderDto> result = orders.stream().map(o -> new OrderDto(o)).collect(Collectors.toList());
        return result;
    }

    /**
     * v4. JPA에서 DTO 직접 조회
     * Query: 루트 1번. 컬렉션 N번 실행됨.
     * 
     */
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4(){
        return orderQueryRepository.findOrderQueryDtos();
    }

    /**
     * v5. JPA에서 DTO 직접 조회 - 최적화
     * Query : 루트 1번. 컬렉션 1번 실행됨.
     * MAP을 사용해서 O(1)
     */
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5(){
        return orderQueryRepository.findAllByDto_optimization();
    }



    @Data
    static class OrderDto{
        private Long orderId;
        private String name;
        private LocalDateTime orderDate; // 주문시간
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order){
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream().map(orderItem -> new OrderItemDto(orderItem)).collect(Collectors.toList());
        }
    }

    @Data
    static class OrderItemDto{
        private String itemName;
        private int orderPrice;
        private int count;
        
        public OrderItemDto(OrderItem orderItem){
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
}
