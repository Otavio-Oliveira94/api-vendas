package com.exemplo.fornecedoresservice.config;

import com.exemplo.fornecedoresservice.model.Fornecedor;
import com.exemplo.fornecedoresservice.repository.FornecedorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {
    private final FornecedorRepository fornecedorRepository;

    public DataInitializer(FornecedorRepository fornecedorRepository) {
        this.fornecedorRepository = fornecedorRepository;
    }

    @Override
    public void run(String... args) {
        if (fornecedorRepository.count() == 0) {
            fornecedorRepository.saveAll(List.of(
                    new Fornecedor(
                            "Fornecedor Alfa",
                            "11.111.111/0001-11"
                    ),
                    new Fornecedor(
                            "Fornecedor Beta",
                            "22.222.222/0001-22"
                    ),
                    new Fornecedor(
                            "Fornecedor Gama",
                            "33.333.333/0001-33"
                    ),
                    new Fornecedor(
                            "Fornecedor Delta",
                            "44.444.444/0001-44"
                    ),
                    new Fornecedor(
                            "Fornecedor Epsilon",
                            "55.555.555/0001-55"
                    )
            ));
        }
    }
}
