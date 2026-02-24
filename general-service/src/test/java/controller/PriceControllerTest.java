package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import general.service.ServiceCheckPrice;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import shared.dto.CheckPrice;
import shared.enums.Banks;
import shared.enums.CurrentPrice;
import shared.enums.TypePrice;
import general.GatewayApplication;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = GatewayApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("PriceController тесты")
class PriceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ServiceCheckPrice serviceCheckPrice;

    @Test
    @DisplayName("Проверка цены успешна")
    void shouldCheckPriceSuccessfully() throws Exception {
        doNothing().when(serviceCheckPrice).checkPrice(eq("test@example.com"), any(CheckPrice.class));

        CheckPrice checkPrice = CheckPrice.of(
                Banks.SBER,
                TypePrice.CURRENCY,
                CurrentPrice.BUY,
                "USD",
                new BigDecimal("90.00")
        );

        mockMvc.perform(post("/check/test@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkPrice)))
                .andExpect(status().isOk())
                .andExpect(content().string("Price check completed"));
    }

    @Test
    @DisplayName("Проверка цены с невалидным email возвращает 400")
    void shouldFailWithInvalidEmail() throws Exception {
        // Настраиваем mock на выброс IllegalArgumentException для невалидного email
        doThrow(new IllegalArgumentException("Invalid email address"))
                .when(serviceCheckPrice).checkPrice(eq("invalid-email"), any(CheckPrice.class));

        CheckPrice checkPrice = CheckPrice.of(
                Banks.SBER,
                TypePrice.CURRENCY,
                CurrentPrice.BUY,
                "USD",
                new BigDecimal("90.00")
        );

        mockMvc.perform(post("/check/invalid-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkPrice)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid price data: Invalid email address"));
    }

    @Test
    @DisplayName("Проверка цены с null телом возвращает 400")
    void shouldFailWithNullBody() throws Exception {
        mockMvc.perform(post("/check/test@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Проверка цены с ошибкой сервиса возвращает 500")
    void shouldFailWithServiceError() throws Exception {
        doThrow(new MessagingException("Email send failed"))
                .when(serviceCheckPrice).checkPrice(eq("test@example.com"), any(CheckPrice.class));

        CheckPrice checkPrice = CheckPrice.of(
                Banks.SBER,
                TypePrice.CURRENCY,
                CurrentPrice.BUY,
                "USD",
                new BigDecimal("90.00")
        );

        mockMvc.perform(post("/check/test@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkPrice)))
                .andExpect(status().isInternalServerError());
    }
}