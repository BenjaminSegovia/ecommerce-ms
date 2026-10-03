package cl.ecommerce.producto_service.service;

import cl.ecommerce.producto_service.dto.ProductoRequestDTO;
import cl.ecommerce.producto_service.dto.ProductoResponseDTO;
import cl.ecommerce.producto_service.exception.RecursoNoEncontradoException;
import cl.ecommerce.producto_service.model.Categoria;
import cl.ecommerce.producto_service.model.Producto;
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
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ProductoService productoService;

    // ---------- helpers ----------

    private Categoria categoria(Long id, String nombre) {
        Categoria c = new Categoria();
        c.setId(id);
        c.setNombre(nombre);
        return c;
    }

    private Producto producto(Long id, Categoria categoria, String nombre, Double precio) {
        Producto p = new Producto();
        p.setId(id);
        p.setVendedorId(1L);
        p.setCategoria(categoria);
        p.setNombre(nombre);
        p.setDescripcion("Descripcion " + nombre);
        p.setPrecio(precio);
        p.setActivo(true);
        return p;
    }

    private ProductoRequestDTO request(Long categoriaId, String nombre, Double precio) {
        ProductoRequestDTO dto = new ProductoRequestDTO();
        dto.setVendedorId(1L);
        dto.setCategoriaId(categoriaId);
        dto.setNombre(nombre);
        dto.setDescripcion("Descripcion " + nombre);
        dto.setPrecio(precio);
        return dto;
    }

    // ---------- listarTodos ----------

    @Test
    void listarTodos_hayProductos_devuelveListaMapeada() {
        Categoria electronica = categoria(1L, "Electronica");
        when(productoRepository.findAll())
                .thenReturn(List.of(producto(1L, electronica, "Notebook", 599990.0)));

        List<ProductoResponseDTO> resultado = productoService.listarTodos();

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        assertEquals("Notebook", resultado.get(0).getNombre());
        assertEquals("Electronica", resultado.get(0).getCategoriaNombre());
    }

    @Test
    void listarTodos_sinProductos_devuelveListaVacia() {
        when(productoRepository.findAll()).thenReturn(List.of());

        List<ProductoResponseDTO> resultado = productoService.listarTodos();

        assertTrue(resultado.isEmpty());
    }

    // ---------- buscarPorId ----------

    @Test
    void buscarPorId_existe_devuelveDTO() {
        Categoria hogar = categoria(2L, "Hogar");
        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto(1L, hogar, "Sofa", 150000.0)));

        ProductoResponseDTO resultado = productoService.buscarPorId(1L);

        assertEquals(1L, resultado.getId());
        assertEquals("Sofa", resultado.getNombre());
        assertEquals(2L, resultado.getCategoriaId());
        assertEquals("Hogar", resultado.getCategoriaNombre());
    }

    @Test
    void buscarPorId_noExiste_lanzaRecursoNoEncontrado() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> productoService.buscarPorId(99L));
    }

    // ---------- buscarPorCategoria ----------

    @Test
    void buscarPorCategoria_devuelveListaFiltrada() {
        Categoria electronica = categoria(1L, "Electronica");
        when(productoRepository.findByCategoriaId(1L))
                .thenReturn(List.of(producto(1L, electronica, "Notebook", 599990.0)));

        List<ProductoResponseDTO> resultado = productoService.buscarPorCategoria(1L);

        assertEquals(1, resultado.size());
        assertEquals("Notebook", resultado.get(0).getNombre());
    }

    // ---------- crear ----------

    @Test
    void crear_categoriaValida_guardaYDevuelveDTO() {
        Categoria electronica = categoria(1L, "Electronica");
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(electronica));
        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(inv -> {
                    Producto p = inv.getArgument(0);
                    p.setId(1L);
                    return p;
                });

        ProductoResponseDTO resultado = productoService.crear(request(1L, "Notebook", 599990.0));

        ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
        verify(productoRepository).save(captor.capture());
        assertEquals("Notebook", captor.getValue().getNombre());
        assertEquals(electronica, captor.getValue().getCategoria());
        assertTrue(captor.getValue().getActivo());
        assertEquals(1L, resultado.getId());
        assertEquals("Electronica", resultado.getCategoriaNombre());
    }

    @Test
    void crear_categoriaInexistente_lanzaRecursoNoEncontrado() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> productoService.crear(request(99L, "Notebook", 599990.0)));

        verify(productoRepository, never()).save(any());
    }

    // ---------- actualizar ----------

    @Test
    void actualizar_datosValidos_actualizaProducto() {
        Categoria viejaCategoria = categoria(1L, "Electronica");
        Categoria nuevaCategoria = categoria(2L, "Hogar");
        Producto existente = producto(1L, viejaCategoria, "Notebook", 599990.0);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(nuevaCategoria));
        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ProductoResponseDTO resultado = productoService.actualizar(
                1L, request(2L, "Notebook Pro", 649990.0));

        assertEquals("Notebook Pro", resultado.getNombre());
        assertEquals(649990.0, resultado.getPrecio());
        assertEquals("Hogar", resultado.getCategoriaNombre());
    }

    @Test
    void actualizar_idInexistente_lanzaRecursoNoEncontrado() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> productoService.actualizar(99L, request(1L, "Notebook", 599990.0)));

        verifyNoInteractions(categoriaRepository);
        verify(productoRepository, never()).save(any());
    }

    @Test
    void actualizar_categoriaInexistente_lanzaRecursoNoEncontrado() {
        Categoria viejaCategoria = categoria(1L, "Electronica");
        Producto existente = producto(1L, viejaCategoria, "Notebook", 599990.0);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> productoService.actualizar(1L, request(99L, "Notebook", 599990.0)));

        verify(productoRepository, never()).save(any());
    }

    // ---------- eliminar ----------

    @Test
    void eliminar_existe_eliminaProducto() {
        when(productoRepository.existsById(1L)).thenReturn(true);

        productoService.eliminar(1L);

        verify(productoRepository).deleteById(1L);
    }

    @Test
    void eliminar_noExiste_lanzaRecursoNoEncontrado() {
        when(productoRepository.existsById(99L)).thenReturn(false);

        assertThrows(RecursoNoEncontradoException.class,
                () -> productoService.eliminar(99L));

        verify(productoRepository, never()).deleteById(any());
    }
}