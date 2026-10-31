package com.onbius.busspass.repository;
import com.onbius.busspass.entity.Movimentacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface MovimentacaoRepository extends JpaRepository<Movimentacao,Long> {
  List<Movimentacao> findByCartaoIdOrderByDataHoraDesc(Long id);
  void deleteByCartaoId(Long id);
}
