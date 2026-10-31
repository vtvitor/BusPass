package com.onbius.busspass.service;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
@Service
public class AuthTokens {
  private final byte[] secret;
  private static final long TTL = 12 * 3600;
  public AuthTokens(@Value("${BUSPASS_AUTH_SECRET:}") String configured) {
    if(configured == null || configured.isBlank()) {
      secret = new byte[32]; new SecureRandom().nextBytes(secret);
    } else {
      if(configured.length()<32) throw new IllegalStateException("BUSPASS_AUTH_SECRET deve ter ao menos 32 caracteres.");
      secret=configured.getBytes(StandardCharsets.UTF_8);
    }
  }
  public long ttl(){return TTL;}
  public String issue(Long id) {
    String data=Base64.getUrlEncoder().withoutPadding().encodeToString(
      (id+":"+(Instant.now().getEpochSecond()+TTL)).getBytes(StandardCharsets.UTF_8));
    return data+"."+sign(data);
  }
  public Long require(HttpServletRequest request) {
    String h=request.getHeader("Authorization");
    if(h==null || !h.startsWith("Bearer ")) throw denied();
    String[] parts=h.substring(7).trim().split("\\.",-1);
    if(parts.length!=2||parts[0].length()>200||parts[1].length()>100) throw denied();
    try {
      if(!MessageDigest.isEqual(sign(parts[0]).getBytes(StandardCharsets.US_ASCII),parts[1].getBytes(StandardCharsets.US_ASCII))) throw denied();
      String[] payload=new String(Base64.getUrlDecoder().decode(parts[0]),StandardCharsets.UTF_8).split(":");
      if(payload.length!=2) throw denied();
      long id=Long.parseLong(payload[0]), exp=Long.parseLong(payload[1]);
      if(id<=0||exp<=Instant.now().getEpochSecond()) throw denied();
      return id;
    }catch(IllegalArgumentException e){throw denied();}
  }
  private String sign(String text){
    try {
      Mac hmac=Mac.getInstance("HmacSHA256");
      hmac.init(new SecretKeySpec(secret,"HmacSHA256"));
      return Base64.getUrlEncoder().withoutPadding().encodeToString(hmac.doFinal(text.getBytes(StandardCharsets.US_ASCII)));
    }catch(Exception e){throw new IllegalStateException("Falha ao gerar sessão",e);}
  }
  private ResponseStatusException denied(){return new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Sessão inválida ou expirada.");}
}
