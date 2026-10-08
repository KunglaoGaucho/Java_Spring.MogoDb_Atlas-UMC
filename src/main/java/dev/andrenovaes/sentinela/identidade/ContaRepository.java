package dev.andrenovaes.sentinela.identidade;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ContaRepository extends MongoRepository<Conta, String> {

    Optional<Conta> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPapel(Papel papel);

    List<Conta> findByPapel(Papel papel, Sort ordem);

    long countByPapel(Papel papel);
}
