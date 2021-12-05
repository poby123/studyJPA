package jpabook.jpashop.repository.order.simplequery;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepository {

    private final EntityManager em;

    /**
     * new 명령어를 통해 JPQL의 결과를 DTO로 즉시 변환.
     * SELECT절에서 원하는 데이터를 직접 선택하므로 DB->에플리케이션 네트워크 용량 최적화(그러나 생각보다는 미비)
     * 리포지토리 재사용성 저하. API 스펙이 리포지토리에 들어가기 때문.
     */
    public List<OrderSimpleQueryDto> findOrderDtos(){
        return em.createQuery(
            "select new jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address)"+
            " from Order o"+
            " join o.member m"+
            " join o.delivery d", OrderSimpleQueryDto.class
        ).getResultList();
    }
}
