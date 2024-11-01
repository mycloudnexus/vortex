package com.consoleconnect.vortex.gateway.service;

import com.consoleconnect.vortex.gateway.entity.OrderEntity;
import com.consoleconnect.vortex.gateway.enums.ResourceTypeEnum;
import com.consoleconnect.vortex.gateway.repo.OrderRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OrderService {
  private OrderRepository orderRepository;

  public List<OrderEntity> listOrders(String orgId) {
    return orderRepository.findByOrganizationId(orgId);
  }

  /**
   * create order
   *
   * @param orgId
   * @param orderId
   * @param resourceType
   * @param resourceId
   * @return
   */
  public OrderEntity createOrder(
      String orgId, String orderId, ResourceTypeEnum resourceType, String resourceId) {

    OrderEntity order = new OrderEntity();
    order.setOrganizationId(orgId);
    order.setOrderId(orderId);
    order.setResourceType(resourceType);
    order.setResourceId(resourceId);
    return orderRepository.save(order);
  }

  /**
   * Fill portId The porId is not generated immediately when an order is created.
   *
   * @param orders
   */
  public void fillOrdersPortId(List<OrderEntity> orders) {
    if (CollectionUtils.isNotEmpty(orders)) {
      orderRepository.saveAll(orders);
    }
  }
}
