package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * X to One
 * Order
 * Order -> Member (ManyToOne)
 * Order -> Delevery (OneToOne)
 * 성능 최적화
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/orders")
public class OrderApiController {

	private final OrderRepository orderRepository;

	@GetMapping("/v1/get")
	public List<Order> ordersV1() {
		// 1. 양방향 매핑에서 걸려오는 것을 다 jsonignore 시켜줘야 한다.
		// 2. 지연 로딩 문제로 인해 가짜 객체들을 끌고 오는 현상, 해결책: hibernate5 build.gradle에 모듈 설치
		List<Order> all = orderRepository.findAllByString(new OrderSearch());

		// EAGER로 하면 굳이 for 문으로 강제 삽입을 할 필요가 없어지지만 성능이 안 좋아진다.
		for (Order order : all) {
			// 실제 유저의 이름을 끌고온다.
			order.getMember().getName(); // Lazy 강제 초기화
			order.getDelivery().getAddress(); // Lazy 강제 초기화
			// Lazy 로딩을 피하기 위해서 EAGER로 바꾸는 건 안된다.
		}

		return all;
	}

	@GetMapping("/v2/get")
	public OrderResult<List<OrderDTO>> ordersV2() {
		// if order result count 2,
		List<Order> orders = orderRepository.findAllByString(new OrderSearch());

		// order 결과가 두번 나온다면, 루프가 한 결과에 또 단 쿼리를 쏴주게 된다.
		// 즉, 쿼리가 order를 부를 때 1번, 결과 개수에 따라 쿼리가 두번씩 호출된다. (쿼리 호출 수: order 호출쿼리 1번 + order 결과 수 * 2)
		// 소위 1+N 문제가 발생
		List<OrderDTO> result = orders.stream()
				.map(o -> new OrderDTO(o))
				.collect(Collectors.toList());
//		System.out.println(result);
		return new OrderResult<>(result);
	}

	// DTO로 바꾸는 일반적인 방법
	@Data
	@AllArgsConstructor
	static class OrderResult<T> {
		private T data;
	}
	@Data
	static class OrderDTO {
		private Long orderId;
		private String name;
		private LocalDateTime orderDate;
		private OrderStatus orderStatus;
		private Address address;

		// constructor
		public OrderDTO(Order order) {
			orderId = order.getId();
			name = order.getMember().getName();  // LAZY 초기화: 영속성 컨텍스트가 member_id를 가지고 영속성 컨텍스트에 찾아본다.
			orderDate = order.getOrderDate();
			orderStatus = order.getStatus();
			address = order.getDelivery().getAddress(); // LAZY 초기화
		}
	}
}
