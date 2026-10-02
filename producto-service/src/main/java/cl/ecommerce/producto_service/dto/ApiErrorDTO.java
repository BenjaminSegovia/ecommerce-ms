package cl.ecommerce.producto_service.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ApiErrorDTO {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String mensaje;
    private String path;
    private Map<String, String> campos; // Nullable: utilizado principalmente para errores de validación (@Valid)
}