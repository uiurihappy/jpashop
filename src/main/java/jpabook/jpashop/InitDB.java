package jpabook.jpashop;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

/**
 * 총 주문 2개
 * userA
 * JPA1 BOOK
 * JPA2 BOOK
 * userB
 * SPRING1 BOOK
 * SPRING2 BOOK
 */
@Component
@RequiredArgsConstructor
public class InitDB {

	private final InitService initService;
	// 서버가 띄워지면 스프링 빈에 엮이고 끝나면
	// PostConstruct가 호출된다.
	@PostConstruct
	public void init(){
		initService.dbInit1();
		initService.dbInit2();
	}

	// 별도의 빈으로 등록
	@Component
	@Transactional
	@RequiredArgsConstructor
	static class InitService {
		private final EntityManager em;

		public void dbInit1 () {
			Member member = new Member();
			member.setName("userA");
			member.setAddress(new Address("서울", "1", "1111"));
			em.persist(member);

			Book book1 = new Book();
			book1.setName("JPA1 BOOK");
			book1.setPrice(10000);
			book1.setStockQuantity(100);

			Book book2 = new Book();
			book2.setName("JPA2 BOOK");
			book2.setPrice(20000);
			book2.setStockQuantity(200);

			em.persist(book1);
			em.persist(book2);

			OrderItem orderItem1 = OrderItem.createOrderItem(book1, 10000, 1);
			OrderItem orderItem2 = OrderItem.createOrderItem(book2, 20000, 2);

			Delivery delivery = new Delivery();
			delivery.setAddress(member.getAddress());
			Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
			em.persist(order);


		}

		// 간지가 없어서 중복도를 위해 메서드를 생성하여
		// 유저, item, 주문 정보를 insert하도록 한다.
		public void dbInit2 () {

			Member member = createMember("userB", "진주", "2", "2222");
			em.persist(member);

			Book book1 = createBook("SPRING1 BOOK", 20000, 200);
			em.persist(book1);
			Book book2 = createBook("SPRING2 BOOK", 40000, 300);
			em.persist(book2);


			Delivery delivery = createDelivery(member);

			OrderItem orderItem1 = OrderItem.createOrderItem(book1, 20000, 3);
			OrderItem orderItem2 = OrderItem.createOrderItem(book2, 40000, 4);

			Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
			em.persist(order);
		}

		private Member createMember(String name, String city, String street, String zipcode) {
			Member member = new Member();
			member.setName(name);
			member.setAddress(new Address(city, street, zipcode));
			return member;
		}
		private Book createBook(String name, int price, int stockQuantity) {
			Book book = new Book();
			book.setName(name);
			book.setPrice(price);
			book.setStockQuantity(stockQuantity);
			return book;
		}
		private Delivery createDelivery(Member member) {
			Delivery delivery = new Delivery();
			delivery.setAddress(member.getAddress());
			return delivery;
		}
	}
}


