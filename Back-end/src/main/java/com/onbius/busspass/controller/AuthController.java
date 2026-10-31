package com.onbius.busspass.controller;
import com.onbius.busspass.entity.Usuario;
import com.onbius.busspass.service.AuthTokens;
import com.onbius.busspass.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final UsuarioService users; private final AuthTokens tokens;
  public AuthController(UsuarioService users,AuthTokens tokens){this.users=users;this.tokens=tokens;}
  @PostMapping("/register")
  public Map<String,Object> register(@RequestBody Usuario data){return result(users.cadastrar(data));}
  @PostMapping("/login")
  public Map<String,Object> login(@RequestBody Map<String,String> data){
    Usuario user=users.login(data.get("email"),data.get("senha"));
    if(user==null)throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"E-mail ou senha inválidos.");
    return result(user);
  }
  private Map<String,Object> result(Usuario u){
    return Map.of("id",u.getId(),"nome",u.getNome(),"email",u.getEmail(),"cpf",u.getCpf(),
      "token",tokens.issue(u.getId()),"expiresInSeconds",tokens.ttl());
  }
}
