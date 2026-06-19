package com.upeu.pagoms.servicio;

import com.upeu.pagoms.evento.EventoPago;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductorPago {

    private final KafkaTemplate<String, EventoPago> kafkaTemplate;
    @Value("${app.kafka.topic.pagos}")
    private String topicPagos;

    public void enviarEventoPago(EventoPago eventoPago) {
        kafkaTemplate.send(topicPagos, String.valueOf(eventoPago.getOrdenId()), eventoPago)
                .whenComplete((resultado, ex) -> {
                    if (ex != null) {
                        log.error(
                                "service=ms-pago component=producer topic={} eventType={} ordenId={} timestamp={} status=error error=\"{}\"",
                                topicPagos,
                                eventoPago.getTipoEvento(),
                                eventoPago.getOrdenId(),
                                eventoPago.getTimestamp(),
                                ex.getMessage()
                        );
                        return;
                    }

                    log.info(
                            "service=ms-pago component=producer topic={} partition={} offset={} eventType={} ordenId={} timestamp={} status=published",
                            resultado.getRecordMetadata().topic(),
                            resultado.getRecordMetadata().partition(),
                            resultado.getRecordMetadata().offset(),
                            eventoPago.getTipoEvento(),
                            eventoPago.getOrdenId(),
                            eventoPago.getTimestamp()
                    );
                });
    }
}
