package controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import user.UserServiceApplication;
import user.service.UserService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = UserServiceApplication.class)
@AutoConfigureMockMvc
@DisplayName("UserController тесты")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("Health check endpoint доступен")
    void shouldReturnHealthOk() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    @DisplayName("Страница логина доступна без аутентификации")
    void shouldShowLoginPageWithoutAuth() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Страница регистрации доступна без аутентификации")
    void shouldShowRegistrationPageWithoutAuth() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Главная страница доступна без аутентификации")
    void shouldShowIndexPageWithoutAuth() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Dashboard требует аутентификации")
    @WithMockUser(username = "test@example.com", roles = "USER")
    void shouldAccessDashboardWithAuth() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk());
    }
}