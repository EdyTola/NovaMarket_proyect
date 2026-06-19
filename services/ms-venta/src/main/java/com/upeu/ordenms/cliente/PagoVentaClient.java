package com.upeu.ordenms.cliente;

import com.upeu.ordenms.dto.MedioPago;
import com.upeu.ordenms.dto.PagoRegistradoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-pago")
public interface PagoVentaClient {

    @PostMapping("/api/v1/pagos/registrar")
    PagoRegistradoDto registrar(@RequestBody RegistrarPagoFeignRequest request);

    record RegistrarPagoFeignRequest(
            Long ventaId,
            Double monto,
            MedioPago medioPago,
            Double montoRecibido
    ) {}
}
