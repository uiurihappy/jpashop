package jpabook.jpashop.repository.order;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

	private final EntityManager em;

	public List<OrderQueryDto> findOrderQueryDtos() {
		List<OrderQueryDto> result = findOrders();

		result.forEach(o -> {
			// order에 orderItems를 직접 넣고 있다.
			// 얘도 order에 따른 N 쿼리 발생
			List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());
			o.setOrderItems(orderItems);
		});

		return result;
	}

	private List<OrderItemQueryDto> findOrderItems(Long orderId) {
		return em.createQuery(
				"select new jpabook.jpashop.repository.order.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
						" from OrderItem oi" +
						" join oi.item i" +
						" where oi.order.id = :orderId", OrderItemQueryDto.class
		)
				.setParameter("orderId", orderId)
				.getResultList();
	}

	private List<OrderQueryDto> findOrders() {
		return em.createQuery(
						// new 연산자 개더러움
						"select new jpabook.jpashop.repository.order.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
								" from Order o" +
								" join o.member m" +
								" join o.delivery d", OrderQueryDto.class
				)
				.getResultList();
	}

	public List<OrderQueryDto> findAllByDto() {
		List<OrderQueryDto> result = findOrders();
		// 주문 번호들을 리스트 화
//		List<Long> orderIds = toOrderIds(result);

		Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(toOrderIds(result));

		result.forEach( o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));

		return result;
	}

	// 분리
	private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {
		// 루프 없이 한방에 가져오기
		List<OrderItemQueryDto> orderItems = em.createQuery(
						"select new jpabook.jpashop.repository.order.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
								" from OrderItem oi" +
								" join oi.item i" +
								" where oi.order.id in :orderIds", OrderItemQueryDto.class
				)
				// 주문 번호들로 조회
				.setParameter("orderIds", orderIds)
				.getResultList();

		// 최적화하여 map으로 바꾼다.
		// 루프를 돌릴 때마다 쿼리를 날렸는데 이번엔 메모리에서 match 해 가지고 set 해준다.
		Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream()
				.collect(Collectors.groupingBy(OrderItemQueryDto::getOrderId));
		return orderItemMap;
	}

	private static List<Long> toOrderIds(List<OrderQueryDto> result) {
		return result.stream()
				.map(o -> o.getOrderId())
				.collect(Collectors.toList());
	}

//	public List<OrderQueryDto> findAllByBto_Flat() {
//
//
//	}
}
