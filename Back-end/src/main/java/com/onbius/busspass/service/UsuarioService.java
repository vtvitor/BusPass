package com.onbius.busspass.service;
import com.onbius.busspass.entity.Usuario;
import com.onbius.busspass.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Locale;
@Service
public class UsuarioService {
  private final UsuarioRepository repo;
  private final BCryptPasswordEncoder bcrypt=new BCryptPasswordEncoder(12);
  public UsuarioService(UsuarioRepository repo){this.repo=repo;}
  public List<Usuario> listar(){return repo.findAll();}
  public Usuario buscarPorId(Long id){return repo.findById(id).orElse(null);}
  @Transactional
  public Usuario cadastrar(Usuario u){
    if(u==null) throw new IllegalArgumentException("Informe os dados do usuário.");
    String name=u.getNome()==null?"":u.getNome().trim();
    String email=u.getEmail()==null?"":u.getEmail().trim().toLowerCase(Locale.ROOT);
    String cpf=u.getCpf()==null?"":u.getCpf().replaceAll("\\D","");
    String password=u.getSenha();
    if(name.length()<3||name.length()>100)throw new IllegalArgumentException("Nome inválido.");
    if(email.length()>100||!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))throw new IllegalArgumentException("E-mail inválido.");
    if(!cpf.matches("\\d{11}"))throw new IllegalArgumentException("CPF inválido.");
    if(password==null||password.length()<8||password.length()>72)throw new IllegalArgumentException("A senha deve ter entre 8 e 72 caracteres.");
    if(repo.findByEmail(email).isPresent()||repo.findByCpf(cpf).isPresent())throw new IllegalArgumentException("E-mail ou CPF já cadastrado.");
    u.setId(null);u.setNome(name);u.setEmail(email);u.setCpf(cpf);u.setSenha(bcrypt.encode(password));
    return repo.save(u);
  }
  @Transactional
  public Usuario login(String email,String password){
    if(email==null||password==null)return null;
    return repo.findByEmail(email.trim().toLowerCase(Locale.ROOT)).filter(u->verifyPassword(u,password)).orElse(null);
  }
  @Transactional
  public boolean verifyPassword(Usuario u,String password){
    if(u==null||password==null||u.getSenha()==null)return false;
    String stored=u.getSenha();
    if(stored.matches("^\\$2[aby]\\$\\d\\d\\$.+"))return bcrypt.matches(password,stored);
    boolean match=MessageDigest.isEqual(stored.getBytes(StandardCharsets.UTF_8),password.getBytes(StandardCharsets.UTF_8));
    if(match){u.setSenha(bcrypt.encode(password));repo.save(u);} // migração de conta antiga ao autenticar
    return match;
  }
}
