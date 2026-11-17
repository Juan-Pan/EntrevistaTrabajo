package com.juanpan.productapi.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductControllerIntegrationTest {

    private static final String PRODUCTS_URL = "/api/products";

    private static final String VALID_PRODUCT = """
            {
              "name": "Monitor",
              "description": "27-pulgadas 4K monitor",
              "price": 1299000.00,
              "stock": 7
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listsSampleProducts() throws Exception {
        mockMvc.perform(get(PRODUCTS_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void createsProductAndReturnsItsLocation() throws Exception {
        mockMvc.perform(post(PRODUCTS_URL).contentType(MediaType.APPLICATION_JSON).content(VALID_PRODUCT))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString(PRODUCTS_URL + "/")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Monitor"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void rejectsInvalidProduct() throws Exception {
        String invalidProduct = """
                { "name": "", "price": -5, "stock": -1 }
                """;

        mockMvc.perform(post(PRODUCTS_URL).contentType(MediaType.APPLICATION_JSON).content(invalidProduct))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.price").exists())
                .andExpect(jsonPath("$.errors.stock").exists());
    }

    @Test
    void returnsProductById() throws Exception {
        mockMvc.perform(get(PRODUCTS_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"));
    }

    @Test
    void returnsNotFoundForUnknownProduct() throws Exception {
        mockMvc.perform(get(PRODUCTS_URL + "/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Product not found"))
                .andExpect(jsonPath("$.detail").value("Product with id 999 was not found"));
    }

    @Test
    void updatesExistingProduct() throws Exception {
        mockMvc.perform(put(PRODUCTS_URL + "/1").contentType(MediaType.APPLICATION_JSON).content(VALID_PRODUCT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Monitor"))
                .andExpect(jsonPath("$.stock").value(7));
    }

    @Test
    void deletesExistingProduct() throws Exception {
        mockMvc.perform(delete(PRODUCTS_URL + "/2"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(PRODUCTS_URL + "/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsNotFoundWhenDeletingUnknownProduct() throws Exception {
        mockMvc.perform(delete(PRODUCTS_URL + "/999"))
                .andExpect(status().isNotFound());
    }
}
