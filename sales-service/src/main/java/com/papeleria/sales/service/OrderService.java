package com.papeleria.sales.service;

import com.papeleria.sales.dto.OrderItemRequest;
import com.papeleria.sales.dto.OrderItemResponse;
import com.papeleria.sales.dto.OrderRequest;
import com.papeleria.sales.dto.OrderResponse;
import com.papeleria.sales.exception.AccessDeniedException;
import com.papeleria.sales.exception.ResourceNotFoundException;
import com.papeleria.sales.model.OrderItem;
import com.papeleria.sales.model.OrderRecord;
import com.papeleria.sales.model.OrderStatus;
import com.papeleria.sales.model.Role;
import com.papeleria.sales.repository.OrderRepository;
import com.papeleria.sales.security.AuthenticatedUser;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<OrderResponse> findAll(AuthenticatedUser currentUser) {
        List<OrderRecord> orders = isAdmin(currentUser)
                ? orderRepository.findAll()
                : orderRepository.findAllByCustomerEmailOrderByCreatedAtDesc(currentUser.email());

        return orders.stream()
                .map(this::mapResponse)
                .toList();
    }

    public OrderResponse findById(Long id, AuthenticatedUser currentUser) {
        return mapResponse(getAuthorizedOrder(id, currentUser));
    }

    public OrderResponse create(OrderRequest request, AuthenticatedUser currentUser) {
        OrderRecord order = new OrderRecord();
        order.setCustomerEmail(currentUser.email());
        order.setCustomerName(currentUser.fullName());
        applyChanges(order, request, currentUser);
        return mapResponse(orderRepository.save(order));
    }

    public OrderResponse update(Long id, OrderRequest request, AuthenticatedUser currentUser) {
        OrderRecord order = getAuthorizedOrder(id, currentUser);
        applyChanges(order, request, currentUser);
        return mapResponse(orderRepository.save(order));
    }

    public void delete(Long id, AuthenticatedUser currentUser) {
        OrderRecord order = getAuthorizedOrder(id, currentUser);
        orderRepository.delete(order);
    }

    private OrderRecord getAuthorizedOrder(Long id, AuthenticatedUser currentUser) {
        OrderRecord order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
        if (!isAdmin(currentUser) && !order.getCustomerEmail().equalsIgnoreCase(currentUser.email())) {
            throw new AccessDeniedException("No tiene permisos para acceder a este pedido");
        }
        return order;
    }

    private void applyChanges(OrderRecord order, OrderRequest request, AuthenticatedUser currentUser) {
        OrderStatus status = request.status() == null ? OrderStatus.PENDING : request.status();
        if (!isAdmin(currentUser) && !(status == OrderStatus.PENDING || status == OrderStatus.CANCELLED)) {
            throw new AccessDeniedException("El usuario USER solo puede usar estados PENDING o CANCELLED");
        }

        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.items()) {
            OrderItem item = new OrderItem();
            item.setProductName(itemRequest.productName().trim());
            item.setQuantity(itemRequest.quantity());
            item.setUnitPrice(itemRequest.unitPrice().setScale(2, RoundingMode.HALF_UP));
            BigDecimal subtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            item.setSubtotal(subtotal);
            items.add(item);
            total = total.add(subtotal);
        }

        order.setStatus(status);
        order.setNotes(request.notes() == null ? null : request.notes().trim());
        order.setTotal(total.setScale(2, RoundingMode.HALF_UP));
        order.replaceItems(items);
    }

    private boolean isAdmin(AuthenticatedUser currentUser) {
        return currentUser.role() == Role.ADMIN;
    }

    private OrderResponse mapResponse(OrderRecord order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerEmail(),
                order.getCustomerName(),
                order.getStatus(),
                order.getNotes(),
                order.getTotal(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getItems().stream()
                        .map(item -> new OrderItemResponse(
                                item.getId(),
                                item.getProductName(),
                                item.getQuantity(),
                                item.getUnitPrice(),
                                item.getSubtotal()
                        ))
                        .toList()
        );
    }
}
