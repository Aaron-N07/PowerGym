package com.ilerna.PowerGym.service;

import com.ilerna.PowerGym.model.Cliente;

import java.util.List;

public interface ClienteService {

    List<Cliente> listarTodos();

    List<Cliente> listarPagina(int pagina, int tamano);

    long contarTodos();

    Cliente buscarPorId(Long id);

    Cliente guardar(Cliente cliente);

    void eliminar(Long id);
}
