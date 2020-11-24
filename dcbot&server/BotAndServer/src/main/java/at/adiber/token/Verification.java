package at.adiber.token;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Verification {
    private String username;
    private String token;
}
