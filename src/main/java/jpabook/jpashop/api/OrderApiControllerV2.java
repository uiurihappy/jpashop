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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
