# Projeto: Integração de Autenticação e API REST no Aplicativo Android (Solução Completa)

## 🚀 Resumo do Projeto e Como Utilizar

A aplicação desenvolvida entrega um sistema completo de gerenciamento de carros, realizando o **CRUD integral** (Create, Read, Update, Delete) através do consumo de uma API Node.js local (`RetrofitClient.kt` e `CarApi.kt`), e fazendo uso extensivo dos serviços do Google para autenticação, armazenamento e geolocalização.

### 🔑 Chaves de Acesso
Para testar o aplicativo, as seguintes chaves foram configuradas e estão embutidas no código:
- **Google Maps API Key:** `${MAPS_API_KEY}` (configurada no `AndroidManifest.xml`)
- **Firebase API Key:** `${FIREBASE_API_KEY}` (configurada no `google-services.json`)

**💡 Como testar:** 
1. Inicie a API Node.js rodando `node index.js` na pasta da API.
2. Inicie o emulador do Android Studio e rode a aplicação.
3. Utilize o número de teste `+55 11 91234-5678` com o código `123456` para contornar o SMS real do Firebase.

---

### 🛠️ Tecnologias e Camadas da Aplicação

1. **Autenticação Segura (Firebase Auth):** 
   Na tela `LoginActivity.kt`, os usuários fazem login usando o número de telefone (Phone Auth). Escolheu-se esse método por ser rápido, seguro e muito comum em apps de mobilidade. O app salva a sessão e pula o login nas próximas aberturas.

2. **Listagem e Renderização de Imagens (Retrofit + Picasso):** 
   A tela principal (`MainActivity.kt`) faz uma requisição `GET` com **Retrofit** para listar os carros. Para a renderização, os itens (`item_car_layout.xml`) são injetados em um `RecyclerView` através do `CarAdapter.kt`. As imagens externas são renderizadas em tempo real usando a biblioteca **Picasso**, escolhida por sua simplicidade e cache automático, garantindo que as imagens não travem a rolagem da tela.

3. **Criação de Carros e Storage de Mídia (Firebase Storage):**
   Ao adicionar um novo carro (`AddCarActivity.kt`), o usuário é obrigado a selecionar uma imagem da galeria. Essa imagem é primeiramente enviada via stream para o **Firebase Storage**. Após o upload, a URL pública gerada pelo Storage é atrelada ao objeto do carro, que então é salvo na API Express via `POST`.

4. **Geolocalização Automática (Fused Location + Maps API):**
   Durante a criação, botões chamam o `FusedLocationProviderClient` para capturar as coordenadas exatas do aparelho via GPS (`Place(lat, long)`). Na tela de leitura (`CarDetailActivity.kt`), essas coordenadas são usadas para criar um marcador estilizado sobre um fragmento nativo do **Google Maps**, proporcionando uma imersão completa sem sair do app.

5. **Update e Deleção (`EditCarActivity.kt`):**
   Acessada pelos botões flutuantes na tela de detalhes, a página de edição espelha a tela de criação, permitindo atualizar qualquer campo (inclusive enviar novas fotos que substituirão as antigas). Ao salvar, efetuamos um `PATCH` na API. Também incluímos um botão crítico de Deleção (`DELETE`), que antes de disparar o endpoint exibirá um `AlertDialog` (pop-up de segurança) exigindo a confirmação do usuário para prevenir apagamentos acidentais.

---

## 📚 Instruções Originais do ExercícioEste projeto envolve a criação de uma tela de login com o Firebase, integração de uma API REST e a exibição de dados em um aplicativo Android. Siga as instruções abaixo para configurar e implementar as funcionalidades solicitadas.