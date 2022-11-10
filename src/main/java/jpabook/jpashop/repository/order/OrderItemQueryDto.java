package jpabook.jpashop.repository.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class OrderItemQueryDto {

	@JsonIgnore
	private Long orderId; // orderId가 필요없으면 JsonIgnore하면 된다.
	private String itemName;
	private int orderPrice;
	private int count;

	public OrderItemQueryDto(Long orderId, String itemName, int orderPrice, int count) {
		this.orderId = orderId;
		this.itemName = itemName;
		this.orderPrice = orderPrice;
		this.count = count;
	}
}
