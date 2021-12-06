package jpabook.jpashop.repository.order.query;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {
    
    private final EntityManager em;

    public List<OrderQueryDto> findOrderQueryDtos(){
        // toOne 관계를 한번에 조회. 최적화하기 쉬우므로 한번에 조회.
        List<OrderQueryDto> result = findOrders();

        // 컬렉션 추가. 추가 쿼리 실행
        result.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());
            o.setOrderItems(orderItems);
        });

        return result;
    }

    /**
     * 최적화. Query : 루트 1번. 컬렉션 1번.
     */
    public List<OrderQueryDto> findAllByDto_optimization(){
        // toOne 관계를 한번에 조회. 최적화하기 쉬우므로 한번에 조회.
        List<OrderQueryDto> result = findOrders();

        // orderItem 컬렉션을 MAP 한번에 조회.
        Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(toOrderIds(result));

        // 루프를 돌면서 컬렉션추가. 추가 쿼리는 실행되지 않음.
        result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));

        return result;
    }


    /**
     * 모든 order들의 id를 리스트로 반환.
     */
    private List<Long> toOrderIds(List<OrderQueryDto> result){
        return result.stream().map(o -> o.getOrderId()).collect(Collectors.toList());
    }


    /**
     * 각각의 order들의 orderItem을 한 번에 조회 후 Map으로 싸서 반환.
     */
    private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds){
        List<OrderItemQueryDto> orderItems = em.createQuery(
            "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)"+
            " from OrderItem oi"+
            " join oi.item i"+
            " where oi.order.id in :orderIds", OrderItemQueryDto.class)
            .setParameter("orderIds", orderIds)
            .getResultList();

        return orderItems.stream().collect(Collectors.groupingBy(OrderItemQueryDto::getOrderId));
    }


    /**
     * 1:N 관계. 즉 컬렉션을 제외한 나머지를 한번에 조회
     */
    private List<OrderQueryDto> findOrders(){
        return em.createQuery(
            "select new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address)"
            + " from Order o"
            + " join o.member m"
            + " join o.delivery d", OrderQueryDto.class)
            .getResultList();
    }

    
    /**
     * 1:N 관계. orderItems 컬렉션 조회
     */
    private List<OrderItemQueryDto> findOrderItems(Long orderId){
        return em.createQuery(
            "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)"+
            " from OrderItem oi"+
            " join oi.item i"+
            " where oi.order.id = : orderId", OrderItemQueryDto.class)
            .setParameter("orderId", orderId)
            .getResultList();
    }




}
