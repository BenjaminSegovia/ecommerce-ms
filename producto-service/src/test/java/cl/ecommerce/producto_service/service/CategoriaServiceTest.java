package cl.ecommerce.producto_service.service;

import cl.ecommerce.producto_service.dto.CategoriaRequestDTO;
import cl.ecommerce.producto_service.dto.CategoriaResponseDTO;
import cl.ecommerce.producto_service.exception.RecursoNoEncontradoException;
import cl.ecommerce.producto_service.exception.ReglaNegocioException;
import cl.ecommerce.producto_service.model.Categoria;
import cl.ecommerce.producto_service.repository.CategoriaRepository;
import cl.ecommerce.producto_service.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    // ---------- helpers ----------

    private Categoria categoria(Long id, String nombre) {
        Categoria c = new Categoria();
        c.setId(id);
        c.setNombre(nombre);
        return c;
    }

    private CategoriaRequestDTO request(String nombre) {
        CategoriaRequestDTO dto = new CategoriaRequestDTO();
        dto.setNombre(nombre);
        return dto;
    }

    // ---------- listarTodas ----------

    @Test
    void listarTodas_hayCategorias_devuelveListaMapeada() {
        when(categoriaRepository.findAll())
                .thenReturn(List.of(categoria(1L, "Electronica"), categoria(2L, "Hogar")));

        List<CategoriaResponseDTO> resultado = categoriaService.listarTodas();

        assertEquals(2, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        assertEquals("Electronica", resultado.get(0).getNombre());
        assertEquals(2L, resultado.get(1).getId());
        assertEquals("Hogar", resultado.get(1).getNombre());
    }

    @Test
    void listarTodas_sinCategorias_devuelveListaVacia() {
        when(categoriaRepository.findAll()).thenReturn(List.of());

        List<CategoriaResponseDTO> resultado = categoriaService.listarTodas();

        assertTrue(resultado.isEmpty());
    }

    // ---------- buscarPorId ----------

    @Test
    void buscarPorId_existe_devuelveDTO() {
        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria(1L, "Hogar")));

        CategoriaResponseDTO resultado = categoriaService.buscarPorId(1L);

        assertEquals(1L, resultado.getId());
        assertEquals("Hogar", resultado.getNombre());
    }

    @Test
    void buscarPorId_noExiste_lanzaRecursoNoEncontrado() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> categoriaService.buscarPorId(99L));
    }

    // ---------- crear ----------

    @Test
    void crear_nombreLibre_guardaYDevuelveDTO() {
        when(categoriaRepository.existsByNombre("Hogar")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class)))
                .thenReturn(categoria(1L, "Hogar"));

        CategoriaResponseDTO resultado = categoriaService.crear(request("Hogar"));

        ArgumentCaptor<Categoria> captor = ArgumentCaptor.forClass(Categoria.class);
        verify(categoriaRepository).save(captor.capture());
        assertEquals("Hogar", captor.getValue().getNombre());
        assertEquals(1L, resultado.getId());
        assertEquals("Hogar", resultado.getNombre());
    }

    @Test
    void crear_nombreDuplicado_lanzaReglaNegocio() {
        when(categoriaRepository.existsByNombre("Hogar")).thenReturn(true);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> categoriaService.crear(request("Hogar")));

        assertTrue(ex.getMessage().contains("Hogar"));
        verify(categoriaRepository, never()).save(any());
    }

    // ---------- actualizar ----------

    @Test
    void actualizar_datosValidos_actualizaNombre() {
        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria(1L, "Viejo")));
        when(categoriaRepository.existsByNombreAndIdNot("Nuevo", 1L)).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CategoriaResponseDTO resultado = categoriaService.actualizar(1L, request("Nuevo"));

        assertEquals(1L, resultado.getId());
        assertEquals("Nuevo", resultado.getNombre());
    }

    @Test
    void actualizar_idInexistente_lanzaRecursoNoEncontrado() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> categoriaService.actualizar(99L, request("Nuevo")));

        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void actualizar_nombreDuplicado_lanzaReglaNegocio() {
        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria(1L, "Viejo")));
        when(categoriaRepository.existsByNombreAndIdNot("Hogar", 1L)).thenReturn(true);

        assertThrows(ReglaNegocioException.class,
                () -> categoriaService.actualizar(1L, request("Hogar")));

        verify(categoriaRepository, never()).save(any());
    }

    // ---------- eliminar ----------

    @Test
    void eliminar_sinProductos_eliminaCategoria() {
        Categoria categoria = categoria(1L, "Hogar");
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(productoRepository.countByCategoriaId(1L)).thenReturn(0L);

        categoriaService.eliminar(1L);

        verify(categoriaRepository).delete(categoria);
    }

    @Test
    void eliminar_noExiste_lanzaRecursoNoEncontrado() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> categoriaService.eliminar(99L));

        verify(categoriaRepository, never()).delete(any());
        verifyNoInteractions(productoRepository);
    }

    @Test
    void eliminar_conProductos_lanzaReglaNegocio() {
        Categoria categoria = categoria(1L, "Hogar");
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(productoRepository.countByCategoriaId(1L)).thenReturn(2L);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> categoriaService.eliminar(1L));

        assertTrue(ex.getMessage().contains("Hogar"));
        assertTrue(ex.getMessage().contains("2"));
        verify(categoriaRepository, never()).delete(any());
    }
}