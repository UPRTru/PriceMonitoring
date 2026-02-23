package user.controller;

import shared.dto.AuthDto;
import shared.dto.CheckPrice;
import shared.enums.Banks;
import shared.enums.CurrentPrice;
import shared.enums.TypePrice;
import shared.exception.BadRequestException;
import shared.exception.NotFoundException;
import user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Objects;

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
                model.addAttribute("error", "Invalid email or password");
                model.addAttribute("user", new RegistrationForm("", "", ""));
                return determineView(all);
            }
        } catch (NotFoundException e) {
            model.addAttribute("error", "User not found");
            model.addAttribute("user", new RegistrationForm("", "", ""));
            return determineView(all);
        } catch (Exception e) {
            model.addAttribute("error", "Login error: " + e.getMessage());
            model.addAttribute("user", new RegistrationForm("", "", ""));
            return determineView(all);
        }
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model,
                                       @RequestParam(required = false, defaultValue = "false") Boolean all) {
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
            model.addAttribute("error", "Validation error");
            model.addAttribute("user", form);
            return determineView(all);
        }
        try {
            userService.register(AuthDto.of(form.email(), form.password()), form.timezone());
            setupSecurityContext(form.email(), request, response);
            request.getSession().setAttribute("email", form.email());
            model.addAttribute("message", "Registration successful! Welcome!");
            return "redirect:/dashboard?all=" + all;
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Error: " + e.getMessage());
            model.addAttribute("user", form);
            return determineView(all);
        } catch (Exception e) {
            model.addAttribute("error", "Registration error: " + e.getMessage());
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
        return "redirect:/login?message=You have successfully logged out";
    }

    @PostMapping("/add/scheduled_price")
    @ResponseBody
    public String addScheduledPrice(@RequestBody CheckPriceRequest request, HttpSession session) {
        String sessionEmail = (String) session.getAttribute("email");
        if (sessionEmail == null || !sessionEmail.equals(request.email())) {
            return "Unauthorized access";
        }
        try {
            Banks bank = Banks.findByName(request.bank())
                    .orElseThrow(() -> new BadRequestException("Invalid bank: " + request.bank()));
            TypePrice typePrice = TypePrice.fromValue(request.typePrice())
                    .orElseThrow(() -> new BadRequestException("Invalid typePrice: " + request.typePrice()));
            CurrentPrice currentPrice = CurrentPrice.fromValue(request.currentPrice())
                    .orElseThrow(() -> new BadRequestException("Invalid currentPrice: " + request.currentPrice()));
            CheckPrice checkPrice = CheckPrice.of(
                    bank,
                    typePrice,
                    currentPrice,
                    request.name(),
                    request.price()
            );
            userService.addScheduledPrice(request.email(), checkPrice);
            return "success";
        } catch (BadRequestException e) {
            return "Validation error: " + e.getMessage();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
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
        } catch (NotFoundException e) {
            return "error: " + e.getMessage();
        } catch (SecurityException e) {
            return "error: unauthorized access";
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
        } catch (NotFoundException e) {
            return "error: " + e.getMessage();
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
                                   @NotBlank @Size(min = 8) String password,
                                   @NotBlank String timezone) {
    }

    public record CheckPriceRequest(String name,
                                    String bank,
                                    String typePrice,
                                    String currentPrice,
                                    BigDecimal price,
                                    String email) {
        public CheckPriceRequest {
            Objects.requireNonNull(name, "Name cannot be null");
            Objects.requireNonNull(bank, "Bank cannot be null");
            Objects.requireNonNull(typePrice, "TypePrice cannot be null");
            Objects.requireNonNull(currentPrice, "CurrentPrice cannot be null");
            Objects.requireNonNull(price, "Price cannot be null");
            Objects.requireNonNull(email, "Email cannot be null");
        }
    }
}