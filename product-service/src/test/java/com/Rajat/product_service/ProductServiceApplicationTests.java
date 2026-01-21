package com.Rajat.product_service;

import java.math.BigDecimal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class ProductServiceApplicationTests {

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ProductRepository productRepository;

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		registry.add("spring..datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
	}

	@Test
	void shouldCreateProduct() throws Exception {
		// --- ARRANGE (Prepare data) ---
		Product productRequest = new Product();
		productRequest.setName("iPhone 15");
		productRequest.setDescription("Latest model");
		productRequest.setPrice(BigDecimal.valueOf(1200));
		productRequest.setStock(100);

		// Convert Object to JSON String
		String productString = objectMapper.writeValueAsString(productRequest);

		// --- ACT (Call the API) ---
		// Simulates: POST http://localhost:8081/api/products
		mockMvc.perform(MockMvcRequestBuilders.post("/api/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(productString))
				.andExpect(MockMvcResultMatchers.status().isCreated()); // Expects HTTP 201

		// --- ASSERT (Verify Database) ---
		// Check if the product was actually saved in the Docker DB
		int currentSize = productRepository.findAll().size();
		Assertions.assertEquals(1, productRepository.findAll().size());

		System.out.println("✅ TEST PASSED! Current Products in DB: " + currentSize);
	}

}
