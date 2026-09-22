# Exercício: Integração de Autenticação e API REST no Aplicativo Android

Este projeto envolve a criação de uma tela de login com o Firebase, integração de uma API REST e a
exibição de dados em um aplicativo Android.
Siga as instruções abaixo para configurar e implementar as funcionalidades solicitadas.

### Resultado do projeto

https://github.com/user-attachments/assets/2649f245-4ea0-41a3-ade1-8e4c7954e349

| Login | Listagem de carro | Criação | Detalhe |
| ----- | ----- | -----| ----|
| <img width="1080" height="2400" alt="Screenshot_20250912_135533" src="https://github.com/user-attachments/assets/2889c2c7-ae22-47cd-b45b-36c6a49f40c7" /> |<img width="1080" height="2400" alt="Screenshot_20250912_135610" src="https://github.com/user-attachments/assets/beaf77ec-4dca-4b47-827d-e0d137516de3" /> |<img width="1080" height="2400" alt="Screenshot_20250912_135737" src="https://github.com/user-attachments/assets/44ae5f3d-76e7-4e36-872c-7f7fd9755fcd" /> | <img width="1080" height="2400" alt="Screenshot_20250912_135752" src="https://github.com/user-attachments/assets/c48ed25e-0a38-42e1-bf50-5c368aa079bb" /> |



### Requisitos

- **IDE**: Android Studio
- **Bibliotecas**: Firebase Authentication, Firebase Storage, Retrofit (para requisições REST),
  Google Maps

## Instruções

### 1. Tela de Login com Firebase

Implemente uma tela de login que use um dos provedores de autenticação do Firebase
(recomenda-se usar autenticação por telefone ou Google).

- **Autenticação por telefone**:
    - Configure o Firebase para aceitar o número de telefone: `+55 11 91234-5678`.
    - Defina o código de verificação para login de teste como `123456`.

- **Autenticação com Google** (caso escolha esta opção) (OPCIONAL):
    - Habilite a autenticação com Google nas configurações do Firebase Console.
    - Implemente a lógica de autenticação no aplicativo, usando o provedor de login do Google.

### 2. Opção de Logout

Adicione uma opção de logout ao aplicativo, permitindo que o usuário saia da conta autenticada. Essa
opção deve estar disponível em uma área visível, como o menu principal ou um botão específico na
interface.

### 3. Integração com API REST `/car`

Implemente a integração com uma API REST disponível
no [Link] (https://github.com/vagnnermartins/FTPR-Car-Api-Node-Express) para exibir e salvar
informações de carros no aplicativo.

- **Estrutura JSON Esperada**:
    ```json
    {
      "imageUrl": "https://image",
      "year": "2020/2020",
      "name": "Gaspar",
      "licence": "ABC-1234",
      "place": {
        "lat": 0,
        "long": 0
      }
    }
    ```

- **Requisitos Específicos**:
    - O campo `imageUrl` deve apontar para uma imagem armazenada no Firebase Storage.
    - Exiba a imagem e as informações de cada carro no aplicativo.
    - Utilize Retrofit para realizar as requisições à API.

### 4. Exibir Localização no Google Maps

Utilize a API do Google Maps para exibir o local (`place`) associado ao carro. Essa localização é
especificada pelos campos `lat` e `long` no JSON da API.

---

### Configuração do Projeto

1. **Firebase**: Configure o projeto com Firebase Authentication e Firebase Storage. Adicione o
   `google-services.json` ao projeto para integração.
2. **Google Maps**: Habilite a API do Google Maps e adicione uma chave de API ao projeto.
3. **Dependências Gradle**:
    - **Firebase**: `Firebase Authentication` e `Firebase Storage`
    - **Retrofit** para a comunicação com a API REST
    - **Picasso** ou outra biblioteca para carregamento de imagens
    - **Google Maps SDK**

### Entrega do Exercício

Aqui está a seção detalhada sobre a entrega do projeto com as instruções para fazer um fork:

---

### Entrega do Projeto

Para enviar o projeto finalizado, siga os passos abaixo:

1. **Faça um Fork do Projeto**:
    - No GitHub, vá até a página do repositório original.
    - Clique em "Fork" no canto superior direito da página para criar uma cópia do repositório em
      seu GitHub pessoal.

2. **Clone o Repositório Forkado**:
    - No seu perfil do GitHub, acesse o repositório forkado.
    - Copie o link de clonagem (HTTPS ou SSH).
    - No terminal, clone o repositório em sua máquina local:
      ```bash
      git clone <link-do-repositorio-forkado>
      ```

3. **Implemente as Funcionalidades**:
    - Siga as instruções do exercício para implementar as funcionalidades.
    - Após concluir, adicione, faça commit e push das alterações para o repositório forkado.

4. **Publicação**:
    - **Opção 1**: Envie o projeto final no seu repositório forkado. Compartilhe o link do
      repositório com o instrutor para avaliação.
    - **Opção 2**: Faça um Pull Request para o repositório original. Para isso:
        - Acesse o repositório original no GitHub.
        - Clique em "Compare & pull request" para iniciar o processo de Pull Request.
        - Descreva as alterações feitas e confirme o envio.

> **Nota**: Certifique-se de que o código está organizado e que todas as funcionalidades foram
> devidamente testadas antes de enviar o projeto.

--- 

Boa entrega e sucesso no desenvolvimento!
