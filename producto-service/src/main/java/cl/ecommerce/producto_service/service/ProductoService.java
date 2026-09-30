package cl.ecommerce.producto_service.service;

import cl.ecommerce.producto_service.dto.ProductoRequestDTO;
import cl.ecommerce.producto_service.dto.ProductoResponseDTO;
import cl.ecommerce.producto_service.model.Categoria;
import cl.ecommerce.producto_service.model.Producto;
import cl.ecommerce.producto_service.repository.CategoriaRepository;
import cl.ecommerce.producto_service.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public List<ProductoResponseDTO> listarTodos() {
        return productoRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public ProductoResponseDTO buscarPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));
        return toResponseDTO(producto);
    }

    public List<ProductoResponseDTO> buscarPorCategoria(Long categoriaId) {
        return productoRepository.findByCategoriaId(categoriaId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public ProductoResponseDTO crear(ProductoRequestDTO dto) {
        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoria no encontrada con id: " + dto.getCategoriaId()));

        Producto producto = new Producto();
        producto.setVendedorId(dto.getVendedorId());
        producto.setCategoria(categoria);
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setActivo(true);

        Producto guardado = productoRepository.save(producto);
        return toResponseDTO(guardado);
    }

    public ProductoResponseDTO actualizar(Long id, ProductoRequestDTO dto) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));

        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoria no encontrada con id: " + dto.getCategoriaId()));

        producto.setVendedorId(dto.getVendedorId());
        producto.setCategoria(categoria);
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());

        Producto actualizado = productoRepository.save(producto);
        return toResponseDTO(actualizado);
    }

    public void eliminar(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new RuntimeException("Producto no encontrado con id: " + id);
        }
        productoRepository.deleteById(id);
    }

    private ProductoResponseDTO toResponseDTO(Producto producto) {
        return new ProductoResponseDTO(
                producto.getId(),
                producto.getVendedorId(),
                producto.getCategoria().getId(),
                producto.getCategoria().getNombre(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getActivo()
        );
    }
}