package cl.ecommerce.producto_service.repository;

import cl.ecommerce.producto_service.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}