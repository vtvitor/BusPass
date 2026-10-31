const API = (() => {
  const BASE = window.BUSPASS_API_URL || "https://backend-projeto-tkvz.onrender.com/api";
  async function call(path, options = {}) {
    const session = (() => {
      try { return JSON.parse(sessionStorage.getItem("buspass.session.v1") || "null"); }
      catch { return null; }
    })();
    let response;
    try {
      response = await fetch(BASE + path, {
        ...options,
        headers: {
          "Content-Type": "application/json",
          ...(session?.token ? { Authorization: "Bearer " + session.token } : {}),
          ...(options.headers || {})
        }
      });
    } catch {
      throw new Error("Sem conexão com a API. Confira sua internet e tente novamente.");
    }
    const text = await response.text();
    let body;
    try { body = text ? JSON.parse(text) : null; } catch { body = null; }
    if (!response.ok) {
      if (response.status === 401 && !path.startsWith("/auth/")) {
        sessionStorage.removeItem("buspass.session.v1");
      }
      throw new Error(body?.message || body?.detail || "Erro " + response.status);
    }
    return body;
  }
  return {
    register: data => call("/auth/register", {method:"POST",body:JSON.stringify(data)}),
    login: data => call("/auth/login", {method:"POST",body:JSON.stringify(data)}),
    listarCartoes: id => call("/cartoes?usuarioId=" + encodeURIComponent(id)),
    criarCartao: data => call("/cartoes", {method:"POST",body:JSON.stringify(data)}),
    recarregarCartao: (id,valorCentavos) => call("/cartoes/"+encodeURIComponent(id)+"/recarga",{
      method:"POST",body:JSON.stringify({valorCentavos})
    }),
    bloquearCartao: (id,senha) => call("/cartoes/"+encodeURIComponent(id)+"/bloquear",{
      method:"PATCH",body:JSON.stringify({senha})
    }),
    historicoCartao: id => call("/cartoes/"+encodeURIComponent(id)+"/historico"),
    deletarCartao: id => call("/cartoes/"+encodeURIComponent(id),{method:"DELETE"}),
    listarLinhas: () => call("/linhas"),
    listarPontos: () => call("/pontos")
  };
})();
