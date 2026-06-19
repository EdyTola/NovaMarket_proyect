package com.upeu.ordenms.repositorio;

import com.upeu.ordenms.entidad.Orden;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdenRepositorio extends JpaRepository<Orden, Long> {

    List<Orden> findAllByOrderByFechaVentaDesc();
}
