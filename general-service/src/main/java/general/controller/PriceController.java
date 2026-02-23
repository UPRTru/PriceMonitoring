package general.controller;

import general.service.ServiceCheckPrice;
import shared.dto.CheckPrice;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Objects;

@Controller
public final class PriceController {

    private final ServiceCheckPrice serviceCheckPrice;

    public PriceController(ServiceCheckPrice serviceCheckPrice) {
        this.serviceCheckPrice = serviceCheckPrice;
    }

    @PostMapping("/check/{email}")
    public ResponseEntity<String> check(@RequestBody CheckPrice checkPrice,
                                        @PathVariable String email) {
        Objects.requireNonNull(checkPrice, "CheckPrice cannot be null");
        Objects.requireNonNull(email, "Email cannot be null");
        try {
            serviceCheckPrice.checkPrice(email, checkPrice);
            return ResponseEntity.ok("Price check completed");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid price data: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error during price check");
        }
    }
}