package com.onbius.busspass.controller;
import com.onbius.busspass.entity.Cartao;
import com.onbius.busspass.entity.Movimentacao;
import com.onbius.busspass.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestController
@RequestMapping("/api/cartoes")
public class CartaoController {
  private final CartaoService cards;private final AuthTokens auth;private final DeviceAuth devices;
  private final UsuarioService users;private final int fare;
  public CartaoController(CartaoService cards,AuthTokens auth,DeviceAuth devices,UsuarioService users,
                          @Value("${BUSPASS_TARIFA_CENTAVOS:500}") int fare){
    this.cards=cards;this.auth=auth;this.devices=devices;this.users=users;this.fare=fare;
  }
  @GetMapping public List<Cartao> listar(@RequestParam(required=false) Long usuarioId,HttpServletRequest req){
    Long me=auth.require(req);
    if(usuarioId!=null&&!me.equals(usuarioId))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Acesso não autorizado.");
    return cards.listarPorUsuario(me);
  }
  @PostMapping public Cartao criar(@RequestBody Cartao dados,HttpServletRequest req){
    Long me=auth.require(req);
    if(dados.getUsuarioId()!=null&&!me.equals(dados.getUsuarioId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Acesso não autorizado.");
    return cards.criar(dados,me);
  }
  @PostMapping("/pagar") public Map<String,Object> pagar(@RequestBody Map<String,Object> dados,HttpServletRequest req){
    devices.require(req);
    int saldo=cards.debitar((String)dados.get("numero"),fare);
    return Map.of("sucesso",true,"novoSaldoCentavos",saldo,"saldoFormatado",String.format("R$ %.2f",saldo/100.0));
  }
  @PostMapping("/{id}/recarga") public Map<String,Integer> recarga(@PathVariable Long id,@RequestBody Map<String,Integer> dados,HttpServletRequest req){
    return Map.of("novoSaldoCentavos",cards.recarregar(id,dados.get("valorCentavos"),auth.require(req)));
  }
  @PatchMapping("/{id}/bloquear") public Map<String,Boolean> bloquear(@PathVariable Long id,@RequestBody Map<String,String> dados,HttpServletRequest req){
    cards.bloquear(id,dados.get("senha"),auth.require(req),users);return Map.of("bloqueado",true);
  }
  @GetMapping("/{id}/historico") public List<Movimentacao> historico(@PathVariable Long id,HttpServletRequest req){
    return cards.historico(id,auth.require(req));
  }
  @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deletar(@PathVariable Long id,HttpServletRequest req){cards.deletar(id,auth.require(req));}
}
