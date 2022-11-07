package jpabook.jpashop.api;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
