package com.onbius.busspass.entity;
import jakarta.persistence.*;
@Entity
@Table(name="cartao")
public class Cartao {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="usuario_id") private Long usuarioId;
  private String numero;
  @Column(name="saldo_centavos") private Integer saldoCentavos=0;
  @Column(name="bloqueado",columnDefinition="boolean default false") private Boolean bloqueado=false;
  public Cartao(){}
  public Long getId(){return id;} public void setId(Long id){this.id=id;}
  public Long getUsuarioId(){return usuarioId;} public void setUsuarioId(Long id){this.usuarioId=id;}
  public String getNumero(){return numero;} public void setNumero(String n){this.numero=n;}
  public Integer getSaldoCentavos(){return saldoCentavos;} public void setSaldoCentavos(Integer s){this.saldoCentavos=s;}
  public Boolean getBloqueado(){return bloqueado;} public void setBloqueado(Boolean b){this.bloqueado=b;}
}
