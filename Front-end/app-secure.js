(() => {
  "use strict";
  const refs = window.BusPassInternals;
  if (!refs) return;
  const { Auth, CardManager } = refs;
  // Sessão por aba: não mantém cópias locais de senhas, cartões e saldo.
  localStorage.removeItem("buspass.users.v1");
  localStorage.removeItem("buspass.session.v1");
  localStorage.removeItem("buspass.cards.v1");
  localStorage.removeItem("buspass.blocked.cards.v1");

  Auth.saveSession = function(session) {
    const expiresAt = Date.now() + Number(session.expiresInSeconds || 43200) * 1000;
    sessionStorage.setItem(this.SESSION_KEY, JSON.stringify({...session, expiresAt}));
  };
  Auth.getSession = function() {
    try {
      const session = JSON.parse(sessionStorage.getItem(this.SESSION_KEY) || "null");
      if (!session?.token || !session.id || !session.email || session.expiresAt <= Date.now()) {
        sessionStorage.removeItem(this.SESSION_KEY);
        return null;
      }
      return session;
    } catch {
      sessionStorage.removeItem(this.SESSION_KEY);
      return null;
    }
  };
  Auth.logout = function() {
    sessionStorage.removeItem(this.SESSION_KEY);
    localStorage.removeItem(this.SESSION_KEY);
  };
  Auth.loadUsers = () => [];
  Auth.register = async function(data) {
    const error = this.validate(data);
    if (error) return { ok:false, msg:error };
    if (data.senha.length < 8) return {ok:false,msg:"A senha deve ter ao menos 8 caracteres."};
    try {
      const response = await API.register({
        nome:data.nome.trim(), email:data.email.trim().toLowerCase(),
        cpf:this.onlyDigits(data.cpf), senha:data.senha
      });
      if (!response.token) throw new Error("O servidor não retornou uma sessão válida.");
      const session = { id:response.id, nome:response.nome, email:response.email,
        cpf:response.cpf, token:response.token, expiresInSeconds:response.expiresInSeconds };
      this.saveSession(session);
      return {ok:true,session};
    } catch(error) {return {ok:false,msg:error.message || "Cadastro indisponível."};}
  };
  Auth.login = async function(email,senha) {
    if (!email || !senha) return {ok:false,msg:"Informe e-mail e senha."};
    try {
      const response = await API.login({email:email.trim().toLowerCase(),senha});
      if (!response.token) throw new Error("Servidor desatualizado: token de acesso ausente.");
      const session = { id:response.id, nome:response.nome, email:response.email,
        cpf:response.cpf, token:response.token, expiresInSeconds:response.expiresInSeconds };
      this.saveSession(session);
      return {ok:true,session};
    }catch(error){return {ok:false,msg:error.message || "Não foi possível entrar."};}
  };

  CardManager.loadLocal = () => [];
  CardManager.saveLocal = () => {};
  CardManager.markBlocked = () => {};
  CardManager.isBlocked = card => Boolean(card && card.bloqueado);
  CardManager.listar = async function() {
    const s=Auth.getSession();
    if(!s) return [];
    const cards=await API.listarCartoes(s.id);
    return cards.map(c=>({...c,nome:s.nome,cpf:s.cpf}));
  };
  CardManager.criar = async function(data) {
    const s=Auth.getSession();
    if(!s) throw new Error("Entre na sua conta para cadastrar um cartão.");
    if(this.onlyDigits(data.cpf) !== this.onlyDigits(s.cpf))
      throw new Error("O CPF do cartão deve ser o da conta conectada.");
    const number=data.number.trim().toUpperCase();
    return API.criarCartao({usuarioId:s.id,numero:number,saldoCentavos:0});
  };
  CardManager.recarregar = async function(id,valor) {
    const amount=this.toCentavos(valor);
    if(amount<=0) return {ok:false,msg:"Informe um valor válido."};
    try {
      const response=await API.recarregarCartao(id,amount);
      return {ok:true,novoSaldo:response.novoSaldoCentavos};
    }catch(error){return {ok:false,msg:error.message||"Não foi possível recarregar."};}
  };
  CardManager.deletar = async function(id) {
    try { await API.deletarCartao(id); return true; }
    catch(error){alert(error.message||"Não foi possível excluir.");return false;}
  };
  CardManager.bloquear = function(id) {
    const modal=document.querySelector("#blockCardModal");
    const password=document.querySelector("#blockCardPassword");
    const confirm=document.querySelector("#confirmBlockBtn");
    const cancel=document.querySelector("#cancelBlockBtn");
    const close=document.querySelector("#closeBlockModal");
    if(!modal||!password||!confirm||!cancel||!close)return Promise.resolve(false);
    modal.style.display="flex";password.value="";password.focus();
    return new Promise(resolve => {
      let busy=false;
      const clean=()=>{modal.style.display="none";
        confirm.removeEventListener("click",accept);
        cancel.removeEventListener("click",dismiss);
        close.removeEventListener("click",dismiss);
      };
      const dismiss=()=>{if(busy)return;clean();resolve(false);};
      const accept=async()=>{
        if(busy)return;
        if(!password.value){alert("Informe sua senha.");return;}
        busy=true;confirm.disabled=true;
        try {
          await API.bloquearCartao(id,password.value);
          clean();resolve(true);
        }catch(error){alert(error.message||"Não foi possível bloquear.");resolve(false);clean();}
        finally{busy=false;confirm.disabled=false;password.value="";}
      };
      confirm.addEventListener("click",accept);
      cancel.addEventListener("click",dismiss);
      close.addEventListener("click",dismiss);
    });
  };
  const renderOriginal=CardManager.render.bind(CardManager);
  CardManager.render=async function(container) {
    try {
      await renderOriginal(container);
      container.querySelectorAll(".card-item").forEach(card=>{
        const actions=card.querySelector(".card-action-buttons");
        if(!actions)return;
        const button=document.createElement("button");
        button.type="button";button.className="btn btn-soft";
        button.textContent="Histórico";button.dataset.historyId=card.dataset.cardId;
        actions.prepend(button);
      });
      if(!container.dataset.historyBound){
        container.dataset.historyBound="true";
        container.addEventListener("click",async event=>{
          const button=event.target.closest("[data-history-id]");
          if(!button)return;
          const card=button.closest(".card-item");
          if(!card)return;
          let area=card.querySelector(".card-history");
          if(area){area.remove();return;}
          area=document.createElement("div");area.className="card-history";
          area.textContent="Carregando histórico...";
          card.appendChild(area);
          try {
            const entries=await API.historicoCartao(button.dataset.historyId);
            area.textContent="";
            const title=document.createElement("strong");
            title.textContent="Últimas movimentações";area.appendChild(title);
            if(!entries.length){
              const empty=document.createElement("p");
              empty.textContent="Ainda não há movimentações registradas nesta versão.";
              area.appendChild(empty);
            }
            entries.slice(0,20).forEach(item=>{
              const row=document.createElement("p");
              const money=(Math.abs(item.valorCentavos)/100).toLocaleString("pt-BR",{style:"currency",currency:"BRL"});
              const date=new Date(item.dataHora).toLocaleString("pt-BR");
              row.textContent=(item.tipo==="RECARGA" ? "Recarga +" : "Passagem -") + money + " · " + date;
              area.appendChild(row);
            });
          }catch(error){area.textContent=error.message||"Histórico indisponível.";}
        });
      }
    }catch(error){
      container.replaceChildren();
      const p=document.createElement("p");p.className="form-error";
      p.textContent=error.message||"Não foi possível carregar seus cartões.";
      container.appendChild(p);
    }
  };
})();
