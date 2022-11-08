package jpabook.jpashop.repository.dtos;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
// DTO 클래스
public class orderDTO {
		private Long orderId;
		private String name;
		private LocalDateTime orderDate;
		private OrderStatus orderStatus;
		private Address address;

		// constructor
		public orderDTO(Long orderId, String name, LocalDateTime orderDate, OrderStatus orderStatus, Address address) {
			this.orderId = orderId;
			this.name = name;
			this.orderDate = orderDate;
			this.orderStatus = orderStatus;
			this.address = address;
		}

}
