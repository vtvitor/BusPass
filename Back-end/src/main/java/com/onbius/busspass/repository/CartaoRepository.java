package com.onbius.busspass.repository;
import com.onbius.busspass.entity.Cartao;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface CartaoRepository extends JpaRepository<Cartao,Long> {
  List<Cartao> findByUsuarioId(Long usuarioId);
  Optional<Cartao> findByNumero(String numero);
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select c from Cartao c where c.id=:id")
  Optional<Cartao> lockedId(@Param("id") Long id);
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select c from Cartao c where c.numero=:numero")
  Optional<Cartao> lockedNumero(@Param("numero") String numero);
}
