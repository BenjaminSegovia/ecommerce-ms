package cl.ecommerce.producto_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ProductoRequestDTO {

    @NotNull(message = "El vendedorId es obligatorio")
    private Long vendedorId;

    @NotNull(message = "La categoriaId es obligatoria")
    private Long categoriaId;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a 0")
    private Double precio;
}