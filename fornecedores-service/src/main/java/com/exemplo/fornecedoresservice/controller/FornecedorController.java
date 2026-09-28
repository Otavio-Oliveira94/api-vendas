package com.exemplo.fornecedoresservice.controller;

import com.exemplo.fornecedoresservice.dto.ProdutoDTO;
import com.exemplo.fornecedoresservice.model.Fornecedor;
import com.exemplo.fornecedoresservice.service.FornecedorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fornecedores")
public class FornecedorController {
    private final FornecedorService fornecedorService;

    public FornecedorController(
            FornecedorService fornecedorService
    ) {
        this.fornecedorService = fornecedorService;
    }

    @GetMapping
    public List<Fornecedor> listarTodos() {
        return fornecedorService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Fornecedor> buscarPorId(@PathVariable("id") Long id) {
        return fornecedorService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Fornecedor> cadastrar(@RequestBody Fornecedor fornecedor) {
        Fornecedor fornecedorSalvo = fornecedorService.salvar(fornecedor);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(fornecedorSalvo);
    }

    @GetMapping("/produtos")
    public List<ProdutoDTO> listarProdutos() {
        return fornecedorService.listarProdutos();
    }
}
