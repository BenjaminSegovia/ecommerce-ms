package cl.ecommerce.producto_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponseDTO {

    private Long id;
    private Long vendedorId;
    private Long categoriaId;
    private String categoriaNombre;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Boolean activo;
}