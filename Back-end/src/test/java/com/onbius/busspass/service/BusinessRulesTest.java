package com.onbius.busspass.service;
import com.onbius.busspass.entity.Cartao;
import com.onbius.busspass.repository.CartaoRepository;
import com.onbius.busspass.repository.MovimentacaoRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
class BusinessRulesTest {
  @Test void sessaoAssinada() {
    AuthTokens tokens=new AuthTokens("uma-chave-forte-para-testes-com-32-caracteres");
    HttpServletRequest req=mock(HttpServletRequest.class);
    when(req.getHeader("Authorization")).thenReturn("Bearer "+tokens.issue(42L));
    assertEquals(42L,tokens.require(req));
  }
  @Test void impedeSaldoInicialArbitrario() {
    CartaoRepository repo=mock(CartaoRepository.class);
    when(repo.save(any(Cartao.class))).thenAnswer(inv->inv.getArgument(0));
    CartaoService service=new CartaoService(repo,mock(MovimentacaoRepository.class));
    Cartao incoming=new Cartao();incoming.setNumero("AB12CD34");incoming.setSaldoCentavos(999999);
    assertEquals(0,service.criar(incoming,1L).getSaldoCentavos());
  }
  @Test void bloqueioEvitaDebito() {
    CartaoRepository repo=mock(CartaoRepository.class);
    Cartao c=new Cartao();c.setBloqueado(true);c.setSaldoCentavos(1000);
    when(repo.lockedNumero("AB12CD34")).thenReturn(Optional.of(c));
    CartaoService service=new CartaoService(repo,mock(MovimentacaoRepository.class));
    assertThrows(ResponseStatusException.class,()->service.debitar("AB12CD34",500));
  }
  @Test void naoRecarregaCartaoDeOutroUsuario() {
    CartaoRepository repo=mock(CartaoRepository.class);
    Cartao c=new Cartao();c.setUsuarioId(1L);
    when(repo.lockedId(5L)).thenReturn(Optional.of(c));
    CartaoService service=new CartaoService(repo,mock(MovimentacaoRepository.class));
    assertThrows(ResponseStatusException.class,()->service.recarregar(5L,1000,2L));
  }
}
