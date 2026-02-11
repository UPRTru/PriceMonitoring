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
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
public class UserController {

    private final UserService userService;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String startPage(HttpSession session, Model model) {
        return startPage(session, model, false);
    }

    @GetMapping("/?all={allModule}")
    public String startPage(HttpSession session, Model model, @PathVariable boolean allModule) {
        String email = (String) session.getAttribute("email");
        if (email != null) {
            return "redirect:/dashboard/?all=" + allModule;
        }
        model.addAttribute("user", new RegistrationForm("", "", ""));
        if (allModule) {
            return "login2";
        } else {
            return "login";
        }
    }

    @GetMapping("/login")
    public String showLoginForm(HttpSession session, Model model) {
        return showLoginForm(session, model, false);
    }

    @GetMapping("/login/?all={allModule}")
    public String showLoginForm(HttpSession session, Model model, @PathVariable boolean allModule) {
        String email = (String) session.getAttribute("email");
        if (email != null) {
            return "redirect:/dashboard/?all=" + allModule;
        }
        model.addAttribute("user", new RegistrationForm("", "", ""));
        if (allModule) {
            return "login2";
        } else {
            return "login";
        }
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpServletRequest request,
                        HttpServletResponse response,
                        Model model) {
        return login(false, username, password, request, response, model);
    }

    @PostMapping("/login/?all={allModule}")
    public String login(@PathVariable boolean allModule,
                        @RequestParam String username,
                        @RequestParam String password,
                        HttpServletRequest request,
                        HttpServletResponse response,
                        Model model) {
        try {
            boolean isAuthenticated = userService.authenticate(username, password);

            if (isAuthenticated) {
                UserDetails userDetails = userService.getByEmail(username);
                Authentication authentication = org.springframework.security.authentication.UsernamePasswordAuthenticationToken
                        .authenticated(userDetails, null, userDetails.getAuthorities());

                SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
                securityContext.setAuthentication(authentication);
                SecurityContextHolder.setContext(securityContext);

                securityContextRepository.saveContext(securityContext, request, response);

                request.getSession().setAttribute("email", username);
                return "redirect:/dashboard/?all=" + allModule;
            } else {
                model.addAttribute("error", "Неверный email или пароль");
                model.addAttribute("user", new RegistrationForm("", "", ""));
                if (allModule) {
                    return "login2";
                } else {
                    return "login";
                }
            }
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при входе: " + e.getMessage());
            model.addAttribute("user", new RegistrationForm("", "", ""));
            if (allModule) {
                return "login2";
            } else {
                return "login";
            }
        }
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        return showRegistrationForm(model, false);
    }

    @GetMapping("/register/?all={allModule}")
    public String showRegistrationForm(Model model, @PathVariable boolean allModule) {
        model.addAttribute("user", new RegistrationForm("", "", ""));
        if (allModule) {
            return "login2";
        } else {
            return "login";
        }
    }

    @PostMapping("/register")
    public String registerUser(@Valid RegistrationForm form,
                               BindingResult result,
                               HttpServletRequest request,
                               HttpServletResponse response,
                               Model model) {
        return registerUser(false, form, result, request, response, model);
    }

    @PostMapping("/register/?all={allModule}")
    public String registerUser(@PathVariable boolean allModule,
                               @Valid RegistrationForm form,
                               BindingResult result,
                               HttpServletRequest request,
                               HttpServletResponse response,
                               Model model) {
        if (result.hasErrors()) {
            model.addAttribute("error", "Ошибка валидации");
            model.addAttribute("user", form);
            if (allModule) {
                return "login2";
            } else {
                return "login";
            }
        }
        try {
            userService.register(new AuthDto(form.email(), form.password()), form.timezone());

            UserDetails userDetails = userService.getByEmail(form.email());
            Authentication authentication = org.springframework.security.authentication.UsernamePasswordAuthenticationToken
                    .authenticated(userDetails, null, userDetails.getAuthorities());

            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);

            securityContextRepository.saveContext(securityContext, request, response);

            request.getSession().setAttribute("email", form.email());
            model.addAttribute("message", "Регистрация успешна! Добро пожаловать!");
            return "redirect:/dashboard/?all=" + allModule;
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка: " + e.getMessage());
            model.addAttribute("user", form);
            if (allModule) {
                return "login2";
            } else {
                return "login";
            }
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        return dashboard(authentication, model, false);
    }

    @GetMapping("/dashboard/?all={allModule}")
    public String dashboard(Authentication authentication, Model model, @PathVariable boolean allModule) {
        if (authentication == null) return "redirect:/login";
        String email = authentication.getName();
        model.addAttribute("email", email);
        model.addAttribute("prices", userService.getScheduledPrice(email));
        if (allModule) {
            return "dashboard2";
        } else {
            return "dashboard";
        }
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
            CheckPrice checkPrice = new CheckPrice(
                    com.precious.shared.enums.Banks.getBanks(request.bank()),
                    com.precious.shared.enums.TypePrice.getTypePrice(request.typePrice()),
                    com.precious.shared.enums.CurrentPrice.getCurrentPrice(request.currentPrice()),
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
    ) {}
}