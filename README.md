# ✅ Checklist do Projeto - Car API Test

## Leonardo Brombilla Antunes

## 📋 Funcionalidades Implementadas

- [x] **Utilização do Compose**
- [x] **Tela de Login com Firebase (Autenticação por Telefone)**
- [x] **Opção de Logout**
- [x] **Integração com API REST `/car` (CRUD Completo)**
- [x] **Firebase Storage para Upload de Imagens**
- [x] **Exibição de Localização no Google Maps**
- [x] **Seleção de Localização no Mapa**


https://github.com/user-attachments/assets/14e3716d-ca50-4bd6-86b1-23d0a91b6a56


## ⚠️ Funcionalidades Pendentes

- [ ] **Autenticação com Google Sign-In**

---

## ⚙️ Configuração Necessária

### 🔥 Firebase

- [x] Projeto criado no Firebase Console
- [x] Authentication habilitado (Phone)
- [x] Storage configurado
- [x] Arquivo `google-services.json` adicionado

### 🗺️ Google Maps

- [x] Maps SDK habilitado no Google Cloud Console
- [x] API Key criada e configurada
- [x] API Key adicionada no `AndroidManifest.xml`

### 🌐 API REST

- [x] API Node.js configurada e rodando
- [x] URL base configurada no app
- [x] Retrofit implementado

---

## 📱 Dados de Teste

### Autenticação Firebase

- **Telefone**: `+55 11 91234-5678`
- **Código**: `123456`

### URLs da API

- **Emulador**: `http://10.0.2.2:3000/`
- **Dispositivo Real**: `http://SEU_IP:3000/`

### Estrutura JSON da API

```json
{
  "id": "1",
  "imageUrl": "https://firebasestorage.googleapis.com/...",
  "year": "2020/2021",
  "name": "Honda Civic",
  "licence": "ABC-1234",
  "place": {
    "lat": -23.5505,
    "long": -46.6333
  }
}
```

---

## 📁 Arquivos de Configuração Essenciais

- [x] `app/google-services.json`
- [x] Google Maps API Key no `AndroidManifest.xml`
- [x] Dependências no `build.gradle`
- [x] Permissões no `AndroidManifest.xml`
