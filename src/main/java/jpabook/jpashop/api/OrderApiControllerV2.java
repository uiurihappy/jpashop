package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/orders")
public class OrderApiControllerV2 {

	private final OrderRepository orderRepository;

	@GetMapping("/v1/getOrders")
	public OrderResult<List<Order>> ordersV1(){
		List<Order> all = orderRepository.findAllByString(new OrderSearch());

		for (Order order : all) {
			order.getMember().getName();
			order.getDelivery().getAddress();

			// orderItems를 가져오고 싶어서 강제로 초기화한 작업
			// 해준 이유는 hibernate5 자체가 lazy loading 했을 때 proxy된 애를 안 뿌려서 강제로 orderItems를 호출
			List<OrderItem> orderItems = order.getOrderItems();

			// orderItem에 item에 이름을 가져오도록 한다.
			orderItems.stream()
					.forEach(o -> o.getItem().getName());
		}

		return new OrderResult<>(all);
	}

	// 아직 1+N 해결 X
	@GetMapping("/v2/getOrders")
	public OrderResult<List<OrderDto>> ordersV2() {
		List<Order> orders = orderRepository.findAllByString(new OrderSearch());

		// 루프 돌리면서 dto로 변환한다.
		List<OrderDto> result = orders.stream()
				.map(o -> new OrderDto(o))
				.collect(Collectors.toList());
		return new OrderResult<>(result);
	}

	// 여기서 중요한 점은 controller 단에서 v2, v3 로직에 대한 차이가 없다.
	// 그렇다면 production 환경이면 repository를 생성히여 데이터를 반환받을 때
	// 일일히 데이터에 알맞는 dto를 선언해줘야 한다.
	@GetMapping("/v3/getOrders")
	public OrderResult<List<OrderDto>> orderV3(){
		List<Order> orders = orderRepository.findAllWithItem(new OrderSearch());

//		for (Order order : orders) {
//			// ref, 참조값까지 같은 것을 확인할 수 있다.
//			System.out.println("order ref =" + order + "id = " + order.getId());
//		}
		// 루프 돌리면서 dto로 변환한다.
		List<OrderDto> result = orders.stream()
				.map(o -> new OrderDto(o))
				.collect(Collectors.toList());

		return new OrderResult<>(result);
	}
	// 단점: 페이징 쿼리가 불가능해진다.
	// 페이징이란? db에서 원하는 데이터 index에서 limit까지 가져오는 것으로 paging 처리한다고 볼 수 있다.

	/**
	 * 	컬렉션을 fetch join 하면 페이징이 불가능하다.
	 * 	일대다 매핑에서 일을 기준으로 페이징을 하는 것이 목적인데 데이터는 N을 기준으로 row가 생성된다.
	 * 	OrderItem을 조인하면 OrderItem이 기준이 되어버린다.
	 * 	해결책
	 * 	1. ToOne 관계는 모두 fetch join을 진행한다.
	 *  2. 컬렉션은 지연 로딩으로 최적화한다.
	 *  3. 지연로딩을 최적화를 위해 hibernate.default_batch_fetch_size, @BatchSize 를 적용한다.
	 *  * hibernate.default_batch_fetch_size는 글로벌 설정
	 *  * @BatchSize는 개별적으로 한 메서드에서만 사용 가능
	 *  * 이 옵션을 사용하면 컬렉션이나, 프록시 객체를 한꺼번에 설정한 size 만큼 IN 쿼리로 조회
	 */
	@GetMapping("/v3.1/getOrders")
	public OrderResult<List<OrderDto>> orderV3_Page(
			@RequestParam(value = "offset", defaultValue = "0") int offset,
			@RequestParam(value = "limit", defaultValue = "100") int limit) {
		List<Order> orders = orderRepository.findAllWithMemberDeliveryV2(offset, limit);

		// 루프 돌리면서 dto로 변환한다.
		List<OrderDto> result = orders.stream()
				.map(o -> new OrderDto(o))
				.collect(Collectors.toList());

		return new OrderResult<>(result);
	}
	/**
	 * 결론
	 * ToOne 관계는 fetch join 해도 페이징에 영향을 주지 않는다.
	 * 따라서 ToOne 관계는 fetch join 으로 쿼리 수를 줄이고 해결하고, 나머지는 hibernate.default_batch_fetch_size로 최적화
	 * 웬만하면 default로 yml에 걸어놓고 개별로 @BatchSize로 설정하는 것이 best라고 생각한다.
	 * size는 100~1000 사이로 설정하는 것을 권장한다. 이유는 batch를 시작하면 db 데이터를 확 size만큼 땡기기에 부하가 걸리지 않는 선에서 책정해야 한다.
	 */

	@Data
	@AllArgsConstructor
	static class OrderResult<T> {
		private T data;
	}

	@Getter
	static class OrderDto {

		private Long orderId;
		private String name;
		private LocalDateTime orderDate;
		private OrderStatus orderStatus;
		private Address address;
		private List<OrderItemDto> orderItems;

		public OrderDto(Order o) {
			// order를 생성자에 넘겨서 반환
			orderId = o.getId();
			name = o.getMember().getName();
			orderDate = o.getOrderDate();
			orderStatus = o.getStatus();
			address = o.getDelivery().getAddress();
			// 강제적으로 호출, 문제점은 엔티티를 모두 노출시킴
//			o.getOrderItems().stream().forEach(o -> o.getItem().getName());
//			orderItems = o.getOrderItems();
			// 그래서 orderItem에 대한 dto 클래스를 선언하여 orderItem 내에 루프 돌리고 orderItemDto로 변환시켜 반환시킨다.
			// 어라..? 쿼리를 날려보니 where in (?,?..) 이런식으로 쿼리가 날라간다. 즉, default_batch_fetch_size로 인해 컬렉션 정보를 이미 알고 있어서 where in으로 맞춰서 쏴준다.
			// default_batch_fetch_size는 where in query 갯수를 몇개로 할건가?
			orderItems = o.getOrderItems().stream()
					.map(orderItem -> new OrderItemDto(orderItem))
					.collect(Collectors.toList());
		}
	}

	@Getter
	static class OrderItemDto {
		/*
		상품명,
		상품 가격,
		주문 수량
		 */
		private String itemName;
		private int orderPrice;
		private int count;

		public OrderItemDto(OrderItem orderItem) {
			itemName = orderItem.getItem().getName();
			orderPrice = orderItem.getOrderPrice();
			count = orderItem.getCount();
		}
	}
}
