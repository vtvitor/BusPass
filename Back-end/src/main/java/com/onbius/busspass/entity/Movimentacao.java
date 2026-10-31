package com.onbius.busspass.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name="movimentacao")
public class Movimentacao {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="cartao_id",nullable=false) private Long cartaoId;
  @Column(nullable=false) private String tipo;
  @Column(name="valor_centavos",nullable=false) private Integer valorCentavos;
  @Column(name="saldo_apos_centavos",nullable=false) private Integer saldoAposCentavos;
  @Column(name="data_hora",nullable=false) private LocalDateTime dataHora;
  protected Movimentacao(){}
  public Movimentacao(Long cartaoId,String tipo,int valor,int saldo){
    this.cartaoId=cartaoId;this.tipo=tipo;this.valorCentavos=valor;this.saldoAposCentavos=saldo;
  }
  @PrePersist public void onCreate(){if(dataHora==null)dataHora=LocalDateTime.now();}
  public Long getId(){return id;} public Long getCartaoId(){return cartaoId;}
  public String getTipo(){return tipo;} public Integer getValorCentavos(){return valorCentavos;}
  public Integer getSaldoAposCentavos(){return saldoAposCentavos;} public LocalDateTime getDataHora(){return dataHora;}
}
