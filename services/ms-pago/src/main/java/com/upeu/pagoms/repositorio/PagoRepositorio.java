package com.upeu.pagoms.repositorio;

import com.upeu.pagoms.entidad.Pago;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoRepositorio extends JpaRepository<Pago, Long> {

    Optional<Pago> findByVentaId(Long ventaId);
}
