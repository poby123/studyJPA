## XtoOne인 경우의 최적화

1. JPQL join
```java
em.createQuery(
            "select o from Order o"+
            " join o.member m"+
            " join o.delivery d", Order.class)
            .getResultList();
```
의 결과 쿼리 ↓
```sql
select
        order0_.order_id as order_id1_6_,
        order0_.delivery_id as delivery4_6_,
        order0_.member_id as member_i5_6_,
        order0_.order_date as order_da2_6_,
        order0_.status as status3_6_ 
    from
        orders order0_ 
    inner join
        member member1_ 
            on order0_.member_id=member1_.member_id 
    inner join
        delivery delivery2_ 
            on order0_.delivery_id=delivery2_.delivery_id
```
=>  member와 delivey를 select 해서 끌고 오지 않기 때문에 N+1 문제가 여전히 발생한다.

<hr/>

2. JPQL jetch join
```java
em.createQuery(
            "select o from Order o"+
            " join fetch o.member m"+
            " join fetch o.delivery d", Order.class)
            .getResultList();
```
의 결과 쿼리 ↓
```sql
select
        order0_.order_id as order_id1_6_0_,
        member1_.member_id as member_i1_4_1_,
        delivery2_.delivery_id as delivery1_2_2_,
        order0_.delivery_id as delivery4_6_0_,
        order0_.member_id as member_i5_6_0_,
        order0_.order_date as order_da2_6_0_,
        order0_.status as status3_6_0_,
        member1_.city as city2_4_1_,
        member1_.street as street3_4_1_,
        member1_.zipcode as zipcode4_4_1_,
        member1_.name as name5_4_1_,
        delivery2_.city as city2_2_2_,
        delivery2_.street as street3_2_2_,
        delivery2_.zipcode as zipcode4_2_2_,
        delivery2_.status as status5_2_2_ 
    from
        orders order0_ 
    inner join
        member member1_ 
            on order0_.member_id=member1_.member_id 
    inner join
        delivery delivery2_ 
            on order0_.delivery_id=delivery2_.delivery_id
```
=>  member와 delivery까지 select 해서 끌고 오기 때문에 N+1 문제가 해결된다.

<hr/>

3. JPQL 사용 DTO로 직접 조회
```java
em.createQuery(
            "select new jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address)"+
            " from Order o"+
            " join o.member m"+
            " join o.delivery d", OrderSimpleQueryDto.class)
```
의 결과 쿼리 ↓
```sql
select
        order0_.order_id as col_0_0_,
        member1_.name as col_1_0_,
        order0_.order_date as col_2_0_,
        order0_.status as col_3_0_,
        delivery2_.city as col_4_0_,
        delivery2_.street as col_4_1_,
        delivery2_.zipcode as col_4_2_ 
    from
        orders order0_ 
    inner join
        member member1_ 
            on order0_.member_id=member1_.member_id 
    inner join
        delivery delivery2_ 
            on order0_.delivery_id=delivery2_.delivery_id
```
=>  필요한 member, delivery의 정보를 select 해서 끌고 오기 때문에 N+1 문제가 해결된다.


<hr/>

## xtoMany 컬렉션의 최적화

1. xtoOne을 먼저 DTO로 가져오기

```java
em.createQuery(
            "select new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address)"
            + " from Order o"
            + " join o.member m"
            + " join o.delivery d", OrderQueryDto.class)
```
의 결과 쿼리 ↓
```sql
select
        order0_.order_id as col_0_0_,
        member1_.name as col_1_0_,
        order0_.order_date as col_2_0_,
        order0_.status as col_3_0_,
        delivery2_.city as col_4_0_,
        delivery2_.street as col_4_1_,
        delivery2_.zipcode as col_4_2_ 
    from
        orders order0_ 
    inner join
        member member1_ 
            on order0_.member_id=member1_.member_id 
    inner join
        delivery delivery2_ 
            on order0_.delivery_id=delivery2_.delivery_id
```

<hr/>

2. xtoMany를 한번에 가져오기

```java
em.createQuery(
            "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)"+
            " from OrderItem oi"+
            " join oi.item i"+
            " where oi.order.id in :orderIds", OrderItemQueryDto.class)
            .setParameter("orderIds", orderIds)
```

의 결과 쿼리 ↓
```sql
    select
        orderitem0_.order_id as col_0_0_,
        item1_.name as col_1_0_,
        orderitem0_.order_price as col_2_0_,
        orderitem0_.count as col_3_0_ 
    from
        order_item orderitem0_ 
    inner join
        item item1_ 
            on orderitem0_.item_id=item1_.item_id 
    where
        orderitem0_.order_id in (
            3 , 7 , 78
        )
```

![test](https://user-images.githubusercontent.com/50279318/144864978-f81989d0-297e-41e4-b5d8-dcea4f8fdd1b.png)
