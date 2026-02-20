package com.precious.user.controller;

import com.precious.shared.dto.AuthDto;
import com.precious.shared.dto.CheckPrice;
import com.precious.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
public final class UserController {

    private final UserService userService;
    private final SecurityContextRepository securityContextRepository;

    public UserController(UserService userService) {
        this.userService = userService;
        this.securityContextRepository = new HttpSessionSecurityContextRepository();
    }

    @GetMapping("/")
    public String startPage(HttpSession session,
                            Model model,
                            @RequestParam(required = false, defaultValue = "false") Boolean all) {
        String email = (String) session.getAttribute("email");
        if (email != null) {
            return "redirect:/dashboard?all=" + all;
        }
        model.addAttribute("user", new RegistrationForm("", "", ""));
        return determineView(all);
    }

    @GetMapping("/login")
    public String showLoginForm(HttpSession session,
                                Model model,
                                @RequestParam(required = false, defaultValue = "false") Boolean all) {
        String email = (String) session.getAttribute("email");
        if (email != null) {
            return "redirect:/dashboard?all=" + all;
        }
        model.addAttribute("user", new RegistrationForm("", "", ""));
        return determineView(all);
    }

    @PostMapping("/login")
    public String login(@RequestParam(required = false, defaultValue = "false") Boolean all,
                        @RequestParam String email,
                        @RequestParam String password,
                        HttpServletRequest request,
                        HttpServletResponse response,
                        Model model) {
        try {
            boolean isAuthenticated = userService.authenticate(email, password);

            if (isAuthenticated) {
                setupSecurityContext(email, request, response);
                request.getSession().setAttribute("email", email);
                return "redirect:/dashboard?all=" + all;
            } else {
                model.addAttribute("error", "Неверный email или пароль");
                model.addAttribute("user", new RegistrationForm("", "", ""));
                return determineView(all);
            }
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при входе: " + e.getMessage());
            model.addAttribute("user", new RegistrationForm("", "", ""));
            return determineView(all);
        }
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model, @RequestParam(required = false, defaultValue = "false") Boolean all) {
        model.addAttribute("user", new RegistrationForm("", "", ""));
        return determineView(all);
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam(required = false, defaultValue = "false") Boolean all,
                               @Valid RegistrationForm form,
                               BindingResult result,
                               HttpServletRequest request,
                               HttpServletResponse response,
                               Model model) {
        if (result.hasErrors()) {
            model.addAttribute("error", "Ошибка валидации");
            model.addAttribute("user", form);
            return determineView(all);
        }
        try {
            userService.register(AuthDto.of(form.email(), form.password()), form.timezone());
            setupSecurityContext(form.email(), request, response);
            request.getSession().setAttribute("email", form.email());
            model.addAttribute("message", "Регистрация успешна! Добро пожаловать!");
            return "redirect:/dashboard?all=" + all;
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка: " + e.getMessage());
            model.addAttribute("user", form);
            return determineView(all);
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication,
                            Model model,
                            @RequestParam(required = false, defaultValue = "false") Boolean all) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String email = authentication.getName();
        model.addAttribute("email", email);
        model.addAttribute("prices", userService.getScheduledPrice(email));
        return determineView(all);
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        SecurityContextHolder.clearContext();
        session.invalidate();
        return "redirect:/login?message=Вы успешно вышли из системы";
    }

    @PostMapping("/add/scheduled_price")
    @ResponseBody
    public String addScheduledPrice(@RequestBody CheckPriceRequest request, HttpSession session) {
        String sessionEmail = (String) session.getAttribute("email");
        if (sessionEmail == null || !sessionEmail.equals(request.email())) {
            return "Неавторизованный доступ";
        }

        try {
            CheckPrice checkPrice = CheckPrice.of(
                    com.precious.shared.enums.Banks.findByName(request.bank()).get(),
                    com.precious.shared.enums.TypePrice.fromValue(request.typePrice()).get(),
                    com.precious.shared.enums.CurrentPrice.fromValue(request.currentPrice()).get(),
                    request.name(),
                    request.price()
            );

            userService.addScheduledPrice(request.email(), checkPrice);
            return "success";
        } catch (Exception e) {
            return "Ошибка: " + e.getMessage();
        }
    }

    @PostMapping("/api/prices/{id}/delete")
    @ResponseBody
    public String deletePrice(@PathVariable Long id, HttpSession session) {
        String email = (String) session.getAttribute("email");
        if (email == null) {
            return "error: not authenticated";
        }

        try {
            userService.deleteScheduledPrice(id, email);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    @PostMapping("/api/users/delete")
    @ResponseBody
    public String deleteUser(HttpSession session) {
        String email = (String) session.getAttribute("email");
        if (email == null) {
            return "error: not authenticated";
        }

        try {
            userService.deleteUser(email);
            SecurityContextHolder.clearContext();
            session.invalidate();
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    private String determineView(Boolean all) {
        return Boolean.TRUE.equals(all) ? "login2" : "login";
    }

    private void setupSecurityContext(String email, HttpServletRequest request, HttpServletResponse response) {
        var userDetails = userService.getByEmail(email);
        var authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        var securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        securityContextRepository.saveContext(securityContext, request, response);
    }

    public record RegistrationForm(@NotBlank @Email String email,
                                   @NotBlank String password,
                                   @NotBlank String timezone) {
    }

    public record CheckPriceRequest(
            String name,
            String bank,
            String typePrice,
            String currentPrice,
            BigDecimal price,
            String email
    ) {
    }
}