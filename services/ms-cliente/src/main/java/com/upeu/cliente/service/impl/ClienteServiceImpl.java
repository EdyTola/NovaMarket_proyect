package com.upeu.cliente.service.impl;

import com.upeu.cliente.dto.ClienteRequest;
import com.upeu.cliente.dto.ClienteResponse;
import com.upeu.cliente.entity.Cliente;
import com.upeu.cliente.repository.ClienteRepository;
import com.upeu.cliente.service.ClienteService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    @Override
    @Transactional
    public ClienteResponse create(ClienteRequest request) {
        if (clienteRepository.findByDocumento(request.getDocumento()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Documento ya registrado");
        }
        Cliente cliente = toEntity(request);
        return toResponse(clienteRepository.save(cliente));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> findAll() {
        return clienteRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse findById(Long id) {
        return toResponse(getById(id));
    }

    @Override
    @Transactional
    public ClienteResponse update(Long id, ClienteRequest request) {
        Cliente cliente = getById(id);
        cliente.setNombre(request.getNombre());
        cliente.setDocumento(request.getDocumento());
        cliente.setEmail(request.getEmail());
        cliente.setTelefono(request.getTelefono());
        if (request.getPuntos() != null) {
            cliente.setPuntos(request.getPuntos());
        }
        return toResponse(clienteRepository.save(cliente));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado");
        }
        clienteRepository.deleteById(id);
    }

    private Cliente getById(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
    }

    private Cliente toEntity(ClienteRequest request) {
        return Cliente.builder()
                .nombre(request.getNombre())
                .documento(request.getDocumento())
                .email(request.getEmail())
                .telefono(request.getTelefono())
                .puntos(request.getPuntos() != null ? request.getPuntos() : 0)
                .build();
    }

    private ClienteResponse toResponse(Cliente cliente) {
        return ClienteResponse.builder()
                .id(cliente.getId())
                .nombre(cliente.getNombre())
                .documento(cliente.getDocumento())
                .email(cliente.getEmail())
                .telefono(cliente.getTelefono())
                .puntos(cliente.getPuntos())
                .build();
    }
}
