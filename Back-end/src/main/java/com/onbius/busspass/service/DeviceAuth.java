package com.onbius.busspass.service;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
@Service
public class DeviceAuth {
  private final String secret;
  public DeviceAuth(@Value("${BUSPASS_DEVICE_TOKEN:}") String secret){this.secret=secret;}
  public void require(HttpServletRequest request){
    if(secret==null||secret.isBlank()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Configure BUSPASS_DEVICE_TOKEN para usar o terminal.");
    String h=request.getHeader("X-Device-Token");
    if(h==null||!MessageDigest.isEqual(secret.getBytes(StandardCharsets.UTF_8),h.getBytes(StandardCharsets.UTF_8)))
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Terminal não autorizado.");
  }
}
