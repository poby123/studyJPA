package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {
    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    /**
     * 엔티티를 fetch join을 사용해서 쿼리 1번에 조회
     * 페치 조인으로 order->member, order->delivery는 이미 조회된 상태이므로 지연로딩x
     * 
     * => N+1 문제 해결!!
     */
    public List<Order> findAllWithMemberDelivery(){
        return em.createQuery(
            "select o from Order o"+
            " join fetch o.member m"+
            " join fetch o.delivery d", Order.class)
            .getResultList();
    }

    /**
     * 컬렉션 페치조인을 사용!
     * 
     * 1대N 조인이 발생하므로 데이터가 예측할 수 없이 증가한다.
     *   => 1이 아닌 N을 기준으로 row가 생성되기 때문이다. 이 때문에 페이징이 불가능하다.
     * 
     * distinct 사용을 통해 SQL에 distict를 추가하고(그러나 DB에서는 모든 컬럼이 같아야만 중복을 제거한다),
     * + 같은 엔티티가 조회되면 애플리케이션에서 중복을 걸러주므로써, order가 중복조회되는 것을 막아준다.
     */
    public List<Order> findAllWithItem(){
        return em.createQuery(
            "select distinct o from Order o"+
            " join fetch o.member m"+
            " join fetch o.delivery d"+
            " join fetch o.orderItems oi"+
            " join fetch oi.item i", Order.class)
            .getResultList();
    }

    public List<Order> findAllWithMemberDelivery(int offset, int limit){
        return em.createQuery(
            "select o from Order o"+
            " join fetch o.member m"+
            " join fetch o.delivery d", Order.class)
            .setFirstResult(offset)
            .setMaxResults(limit)
            .getResultList();
    }


    /*
     * JPQL 로 처리하는 방법.
     * 번거롭고, 실수가 발생하기 쉽다.
     * */
    public List<Order> findAllByString(OrderSearch orderSearch) {
        String jpql = "select o From Order o join o.member m";
        boolean isFirstCondition = true;

        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }

        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }
        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000); //최대 1000건
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }
        return query.getResultList();
    }

    /*
     * JPA Criteria 로 처리하는 방법
     * 실무에서 사용하기에는 복잡하다.
     * */
    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Order, Member> m = o.join("member", JoinType.INNER); //회원과 조인
        List<Predicate> criteria = new ArrayList<>();

        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"), orderSearch.getOrderStatus());
            criteria.add(status);
        }

        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name = cb.like(m.<String>get("name"), "%" + orderSearch.getMemberName() + "%");
            criteria.add(name);
        }
        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000); //최대1000 건
        return query.getResultList();
    }

    /*
    * QueryDSL 로 처리.
    * 실무에서 사용하기 적합하다.
    * */
}
