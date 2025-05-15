package org.example.springorderdocker.service;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.example.springorderdocker.entity.Order;
import org.example.springorderdocker.repo.OrderRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@AllArgsConstructor
@NoArgsConstructor
@Service
public class OrderService {

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private RestTemplate restTemplate;

    public List<Order> getAllOrders() {
        return orderRepo.findAll();
    }

    public Order insertOrder(Order order) {
        if (validateOrderProductAndInventory(order)) {
            order.setStatus("CONFIRMED");
            return orderRepo.save(order);
        }
        else
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order validation failed.");
    }

    public Optional<Order> getOrderById(Integer id) {
        return orderRepo.findById(id);
    }

    public void deleteOrderById(Integer id) {
        orderRepo.deleteById(id);
    }

    public Order updateOrder(Integer id, Order updatedOrder) {
        Order existingOrder = orderRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found with id: " + id));

        existingOrder.setProductId(updatedOrder.getProductId());
        existingOrder.setQuantity(updatedOrder.getQuantity());
        existingOrder.setOrderDate(updatedOrder.getOrderDate());
        existingOrder.setTotalAmount(updatedOrder.getTotalAmount());
        existingOrder.setStatus(updatedOrder.getStatus());
        existingOrder.setCreatedAt(updatedOrder.getCreatedAt());

        return orderRepo.save(existingOrder);
    }

    public boolean validateOrderProductAndInventory(Order order) {
        try {
            Integer productId = order.getProductId();
            Integer requestedQty = order.getQuantity();

            // Step 1: Validate product exists and get price
            String productUrl = "http://spring-product-docker:8080/api/products/findById?id=" + productId;
            ResponseEntity<Map> productResponse = restTemplate.getForEntity(productUrl, Map.class);

            System.out.println("Checking product existence for ID: " + productId +
                " | Status: " + productResponse.getStatusCode());

            if (!productResponse.getStatusCode().is2xxSuccessful() || productResponse.getBody() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product with ID " + productId + " does not exist.");
            }

            Map<String, Object> productData = productResponse.getBody();
            Object priceObj = productData.get("price");

            if (priceObj == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing price in product data for ID " + productId);
            }

            var price = new BigDecimal(priceObj.toString());
            var totalAmount = price.multiply(BigDecimal.valueOf(requestedQty));
            order.setTotalAmount(totalAmount);

            System.out.println("Product price: " + price + " | Total amount: " + totalAmount);

            // Step 2: Validate inventory
            String inventoryUrl = "http://spring-inventory-docker:8080/api/inventory/findById?id=" + productId;
            ResponseEntity<Map> inventoryResponse = restTemplate.getForEntity(inventoryUrl, Map.class);

            System.out.println("Checking inventory for product ID: " + productId +
                " | Status: " + inventoryResponse.getStatusCode());

            if (!inventoryResponse.getStatusCode().is2xxSuccessful() || inventoryResponse.getBody() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Inventory not found for product ID " + productId);
            }

            Map<String, Object> inventory = inventoryResponse.getBody();
            Integer quantityAvailable = (Integer) inventory.get("quantityAvailable");
            Integer inventoryId = (Integer) inventory.get("id");

            System.out.println("Inventory details - Product ID: " + productId +
                " | Inventory ID: " + inventoryId +
                " | Available: " + quantityAvailable + " | Requested: " + requestedQty);

            if (quantityAvailable == null || inventoryId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing data in inventory response.");
            }

            if (quantityAvailable < requestedQty) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient inventory for product ID " + productId +
                    ". Available: " + quantityAvailable + ", Requested: " + requestedQty);
            }

            // Step 3: Update inventory
            Integer newQuantity = quantityAvailable - requestedQty;
            inventory.put("quantityAvailable", newQuantity);
            inventory.put("lastUpdated", Instant.now().toString());

            String updateUrl = "http://spring-inventory-docker:8080/api/inventory/update";
            restTemplate.put(updateUrl, inventory);

            System.out.println("Inventory updated for product ID: " + productId +
                " | New quantity: " + newQuantity);

            return true;

        } catch (Exception e) {
            System.err.println(" Order validation failed: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order validation failed: " + e.getMessage());
        }
    }



}
