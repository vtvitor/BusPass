package com.onbius.busspass.service;
import com.onbius.busspass.entity.Cartao;
import com.onbius.busspass.entity.Movimentacao;
import com.onbius.busspass.repository.CartaoRepository;
import com.onbius.busspass.repository.MovimentacaoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Locale;
@Service
public class CartaoService {
  private final CartaoRepository cards; private final MovimentacaoRepository moves;
  public CartaoService(CartaoRepository cards,MovimentacaoRepository moves){this.cards=cards;this.moves=moves;}
  public List<Cartao> listarPorUsuario(Long id){return cards.findByUsuarioId(id);}
  @Transactional
  public Cartao criar(Cartao incoming,Long owner){
    if(incoming==null||incoming.getNumero()==null)throw new IllegalArgumentException("Informe o número do cartão.");
    String n=incoming.getNumero().trim().toUpperCase(Locale.ROOT);
    if(!n.matches("[A-Z0-9]{6,30}"))throw new IllegalArgumentException("Número do cartão inválido.");
    if(cards.findByNumero(n).isPresent())throw new ResponseStatusException(HttpStatus.CONFLICT,"Cartão já cadastrado.");
    Cartao c=new Cartao();c.setNumero(n);c.setUsuarioId(owner);c.setSaldoCentavos(0);c.setBloqueado(false);
    return cards.save(c);
  }
  private Cartao ownedLocked(Long cardId,Long owner){
    Cartao c=cards.lockedId(cardId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Cartão não encontrado."));
    if(!owner.equals(c.getUsuarioId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Este cartão não pertence à sua conta.");
    return c;
  }
  @Transactional
  public int recarregar(Long id,Integer cents,Long owner){
    if(cents==null||cents<=0||cents>100000)throw new IllegalArgumentException("Recarga demonstrativa deve ser entre R$ 0,01 e R$ 1.000,00.");
    Cartao c=ownedLocked(id,owner);
    if(Boolean.TRUE.equals(c.getBloqueado()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Cartão bloqueado.");
    int old=c.getSaldoCentavos()==null?0:c.getSaldoCentavos();
    if(old>100000000-cents)throw new IllegalArgumentException("Saldo máximo atingido.");
    int value=old+cents;c.setSaldoCentavos(value);
    moves.save(new Movimentacao(c.getId(),"RECARGA",cents,value));return value;
  }
  @Transactional
  public int debitar(String number,int fare){
    if(number==null||!number.matches("[A-Za-z0-9]{6,30}"))throw new IllegalArgumentException("Cartão inválido.");
    if(fare<=0||fare>100000)throw new IllegalArgumentException("Tarifa inválida.");
    Cartao c=cards.lockedNumero(number.trim().toUpperCase(Locale.ROOT))
      .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Cartão RFID não cadastrado."));
    if(Boolean.TRUE.equals(c.getBloqueado()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Cartão bloqueado.");
    int old=c.getSaldoCentavos()==null?0:c.getSaldoCentavos();
    if(old<fare)throw new ResponseStatusException(HttpStatus.CONFLICT,"Saldo insuficiente.");
    int value=old-fare;c.setSaldoCentavos(value);
    moves.save(new Movimentacao(c.getId(),"PASSAGEM",-fare,value));return value;
  }
  @Transactional
  public void bloquear(Long id,String password,Long owner,UsuarioService users){
    Cartao c=ownedLocked(id,owner);
    if(password==null||!users.verifyPassword(users.buscarPorId(owner),password))
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Senha incorreta.");
    c.setBloqueado(true);
  }
  @Transactional(readOnly=true)
  public List<Movimentacao> historico(Long id,Long owner){
    Cartao c=cards.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Cartão não encontrado."));
    if(!owner.equals(c.getUsuarioId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Acesso não autorizado.");
    return moves.findByCartaoIdOrderByDataHoraDesc(id);
  }
  @Transactional
  public void deletar(Long id,Long owner){Cartao c=ownedLocked(id,owner);moves.deleteByCartaoId(id);cards.delete(c);}
}
