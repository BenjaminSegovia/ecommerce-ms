package cl.ecommerce.producto_service.service;

import cl.ecommerce.producto_service.dto.CategoriaRequestDTO;
import cl.ecommerce.producto_service.dto.CategoriaResponseDTO;
import cl.ecommerce.producto_service.exception.RecursoNoEncontradoException;
import cl.ecommerce.producto_service.exception.ReglaNegocioException;
import cl.ecommerce.producto_service.model.Categoria;
import cl.ecommerce.producto_service.repository.CategoriaRepository;
import cl.ecommerce.producto_service.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public List<CategoriaResponseDTO> listarTodas() {
        return categoriaRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public CategoriaResponseDTO buscarPorId(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada con id: " + id));
        return toResponseDTO(categoria);
    }

    public CategoriaResponseDTO crear(CategoriaRequestDTO dto) {
        if (categoriaRepository.existsByNombre(dto.getNombre())) {
            throw new ReglaNegocioException("Ya existe una categoría con el nombre: " + dto.getNombre());
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(dto.getNombre());

        Categoria guardada = categoriaRepository.save(categoria);
        return toResponseDTO(guardada);
    }

    public CategoriaResponseDTO actualizar(Long id, CategoriaRequestDTO dto) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada con id: " + id));

        if (categoriaRepository.existsByNombreAndIdNot(dto.getNombre(), id)) {
            throw new ReglaNegocioException("Ya existe una categoría con el nombre: " + dto.getNombre());
        }

        categoria.setNombre(dto.getNombre());

        Categoria actualizada = categoriaRepository.save(categoria);
        return toResponseDTO(actualizada);
    }

    public void eliminar(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada con id: " + id));

        long productosAsociados = productoRepository.countByCategoriaId(id);
        if (productosAsociados > 0) {
            throw new ReglaNegocioException("No se puede eliminar la categoría '" + categoria.getNombre() + "' porque tiene " + productosAsociados + " productos asociados");
        }

        categoriaRepository.delete(categoria);
    }

    private CategoriaResponseDTO toResponseDTO(Categoria categoria) {
        return new CategoriaResponseDTO(
                categoria.getId(),
                categoria.getNombre()
        );
    }
}