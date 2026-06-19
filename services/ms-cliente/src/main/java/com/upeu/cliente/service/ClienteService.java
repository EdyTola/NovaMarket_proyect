package com.upeu.cliente.service;

import com.upeu.cliente.dto.ClienteRequest;
import com.upeu.cliente.dto.ClienteResponse;
import java.util.List;

public interface ClienteService {

    ClienteResponse create(ClienteRequest request);

    List<ClienteResponse> findAll();

    ClienteResponse findById(Long id);

    ClienteResponse update(Long id, ClienteRequest request);

    void delete(Long id);
}
