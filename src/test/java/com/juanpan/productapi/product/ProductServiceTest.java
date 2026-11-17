package com.juanpan.productapi.product;

import com.juanpan.productapi.product.dto.ProductRequest;
import com.juanpan.productapi.product.dto.ProductResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    private static final Long MISSING_ID = 99L;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void findAllReturnsEveryProduct() {
        when(productRepository.findAll()).thenReturn(List.of(aProduct("Mouse"), aProduct("Keyboard")));

        List<ProductResponse> products = productService.findAll();

        assertThat(products).extracting(ProductResponse::name).containsExactly("Mouse", "Keyboard");
    }

    @Test
    void findByIdThrowsWhenProductDoesNotExist() {
        when(productRepository.findById(MISSING_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(MISSING_ID))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createSavesTheProduct() {
        ProductRequest request = new ProductRequest("Mouse", "Wireless", new BigDecimal("89900.00"), 5);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse created = productService.create(request);

        assertThat(created.name()).isEqualTo("Mouse");
        assertThat(created.price()).isEqualByComparingTo("89900.00");
        assertThat(created.stock()).isEqualTo(5);
    }

    @Test
    void updateChangesEveryField() {
        Product existing = aProduct("Mouse");
        ProductRequest request = new ProductRequest("Gaming mouse", "RGB", new BigDecimal("120000.00"), 3);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.saveAndFlush(existing)).thenReturn(existing);

        ProductResponse updated = productService.update(1L, request);

        assertThat(updated.name()).isEqualTo("Gaming mouse");
        assertThat(updated.description()).isEqualTo("RGB");
        assertThat(updated.price()).isEqualByComparingTo("120000.00");
        assertThat(updated.stock()).isEqualTo(3);
    }

    @Test
    void updateThrowsWhenProductDoesNotExist() {
        ProductRequest request = new ProductRequest("Mouse", null, BigDecimal.TEN, 1);
        when(productRepository.findById(MISSING_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(MISSING_ID, request))
                .isInstanceOf(ProductNotFoundException.class);
        verify(productRepository, never()).saveAndFlush(any());
    }

    @Test
    void deleteRemovesExistingProduct() {
        Product existing = aProduct("Mouse");
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));

        productService.delete(1L);

        verify(productRepository).delete(existing);
    }

    @Test
    void deleteThrowsWhenProductDoesNotExist() {
        when(productRepository.findById(MISSING_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(MISSING_ID))
                .isInstanceOf(ProductNotFoundException.class);
        verify(productRepository, never()).delete(any());
    }

    private static Product aProduct(String name) {
        return new Product(name, "Description", new BigDecimal("1000.00"), 10);
    }
}
