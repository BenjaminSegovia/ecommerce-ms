package cl.ecommerce.producto_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorDTO {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String mensaje;
    private String path;
    private Map<String, String> campos;
}