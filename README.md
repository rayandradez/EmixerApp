# Equalizador de Áudio para Carro - Aplicativo Android

**Um aplicativo Android personalizável para equalização de áudio precisa.**

## Equipe

* Alisson Freitas
* Eduardo Perez Uanús
* João Gabriel A. Gomes Alves
* Rayanne da Silva Andrade

## Visão Geral

Este projeto é um aplicativo Android nativo desenvolvido como parte de um curso de pós-graduação em Sistemas Embarcados (foco em Desenvolvimento Android). Embora uma versão futura vise a integração direta com sistemas de áudio automotivos, a versão atual fornece uma experiência independente em um smartphone Android, permitindo que os usuários criem e gerenciem perfis de equalização de áudio personalizados. Isso permite o teste e desenvolvimento da funcionalidade principal antes da integração com o sistema do veículo.

O aplicativo aborda o problema de equalizadores de áudio embarcados limitados ou mal projetados. Muitos veículos oferecem controle insuficiente, deixando os usuários frustrados em suas tentativas de otimizar a experiência auditiva. Nosso aplicativo oferece uma solução flexível e intuitiva.

## Recursos Principais

* **Gerenciamento de Usuários:**
    * **Seleção de Perfil:** Alterne facilmente entre perfis de usuário existentes;
    * **Criação/Edição de Usuário:** Adicione, edite (incluindo nome e ícone do perfil) e exclua perfis de usuário;
    * **Importação de Perfis:** Importe os perfis dos seus contatos salvos para dentro do app;
    * **Aviso de Modo Avião:** Avisamos quando o Modo Avião está ativo ou inativo. Pois a sincronização entre os dispositivos não é possível se eles estiverem em Modo Avião.
* **Equalizador Intuitivo:** 
    * **Controle Preciso:** Ajuste os parâmetros de áudio com um mixer amigável.
    * **Graves (Low):** Controle de frequências baixas (aprox. 100Hz e abaixo, faixa de ajuste de -15dB a +15dB).
    * **Médias (Mid):** Ajuste de frequências médias (aprox. 1kHz a 4kHz, faixa de ajuste de -15dB a +15dB).
    * **Agudos (High):** Controle de frequências altas (acima de 4kHz, faixa de ajuste de -15dB a +15dB).
    * **Balanceamento Estéreo (Pan):** Ajuste o balanceamento estéreo.
    * **Volume Geral (Main):** Controle do volume total de saída.
    * **Salvar/Redefinir:** Salve as configurações personalizadas do equalizador no perfil selecionado ou redefina para os valores padrão.

## Detalhes Técnicos

Este aplicativo utiliza as seguintes tecnologias:

* **Arquitetura:** Model-View-ViewModel (MVVM)
* **Navegação:** Navigation Graph
* **Banco de Dados:** Room Persistence Library, Firebase
* **Tratamento do Modo Avião:** Broadcast Receivers
* **Importação de perfis:** Content Provider
* **Injeção de Dependências:** Gerenciamento manual de dependências.
* **Link do Figma:** [Interface no Figma](https://www.figma.com/design/ahaN4lSCMNpfLZp3OSnHPf/Emixer?node-id=0-1&p=f&t=YtUwxEKy6SIWXL8c-0)

# Permissões do Aplicativo

Para garantir o funcionamento completo e eficaz do aplicativo, são necessárias algumas permissões específicas. Abaixo estão listadas as permissões essenciais e suas funcionalidades:

## Permissão para Acesso a Arquivos de Áudio

Para ajustar os parâmetros de equalização de forma eficaz, o aplicativo precisa acessar os arquivos de áudio do dispositivo.

- **Permissão Solicitada:** `android.permission.READ_MEDIA_AUDIO`
- **Função:** Garante acesso somente à leitura dos arquivos de mídia de áudio, sem acessar outros dados do dispositivo.
- **Importância:** Sem esta permissão, o aplicativo não poderá ajustar as configurações de equalização.
- **Privacidade:** Não coleta informações pessoais.

## Permissão para Modificar Configurações de Áudio

O aplicativo tem a capacidade de modificar as configurações de áudio globais.

- **Permissão Incluída:** `android.permission.MODIFY_AUDIO_SETTINGS`
- **Função:** Permite que o aplicativo modifique as configurações de áudio globais, como o equalizador.
- **Concessão:** Esta permissão é concedida automaticamente no momento da instalação, não requer solicitação em tempo de execução (runtime).

## Permissão de Acesso à Lista de Contatos

Para permitir a importação de perfis, o aplicativo requer acesso à lista de contatos do dispositivo.

- **Permissão Solicitada:** `android.permission.READ_CONTACTS`
- **Função:** Garante acesso somente à leitura da lista de contatos, sem acessar outros dados do dispositivo.
- **Importância:** Sem esta permissão, o aplicativo não poderá importar as configurações para a criação do perfil.
- **Privacidade:** Não coleta informações pessoais.

Essas permissões são fundamentais para oferecer uma experiência de uso otimizada e personalizável aos usuários.


## Testes

Para garantir a alta qualidade e confiabilidade do aplicativo, foram implementados testes unitários e instrumentais abrangentes.  A estratégia de teste visa cobrir tanto a lógica interna do aplicativo quanto a interação do usuário com a interface.

### Testes Unitários

Os testes unitários validam a funcionalidade individual de componentes menores do código, assegurando que cada parte opere conforme o esperado.  Utilizamos o MockK para simular dependências e o Kotlin Coroutines Test para controlar o escopo de execução das corrotinas.  Os testes unitários foram focados principalmente no `MainViewModel`, que gerencia a lógica de negócio principal do aplicativo.

**Casos de Teste do `MainViewModel`:**

* **`initialState_shouldHaveEmptyUserList()`:** Verifica se a lista de usuários está vazia ao inicializar o `ViewModel`.
* **`addUser_shouldAddUserToList()`:** Verifica se um novo usuário é adicionado corretamente à lista.
* **`addMultipleUsers_shouldContainAllUsers()`:** Verifica se múltiplos usuários são adicionados e mantidos na lista.
* **`addAndRemoveUser_shouldHandleCorrectly()`:** Verifica se a adição e remoção de usuários são executadas corretamente.


### Testes Instrumentais (UI)

Os testes instrumentais utilizam o Espresso para simular interações do usuário com a interface do aplicativo em um ambiente real (ou simulado). Eles validam fluxos de trabalho completos, garantindo que a experiência do usuário seja fluida e intuitiva. Os testes foram focados nas telas críticas de interação com o usuário, assegurando a correta funcionalidade das telas, incluindo o `AddUser` and `ManageUser` Fragment.


**Casos de Teste Instrumentais:**

* **`addUserTest()`:** Verifica o fluxo completo de adicionar um novo usuário, incluindo navegação entre telas, entrada de dados e validação.
* **`editUserTest()`:** Verifica o fluxo de edição de um usuário existente, incluindo a atualização de dados e a validação das mudanças.
* **`deleteUserTest()`:** Verifica o fluxo de exclusão de um usuário, incluindo a confirmação da ação e a remoção do usuário da lista.


### Estratégia de Testes:

A combinação de testes unitários e instrumentais oferece uma cobertura de teste robusta.  Testes unitários garantem a correta funcionalidade da lógica interna, enquanto os testes instrumentais validam a interação do usuário com a interface e a integração entre diferentes componentes.  Esta abordagem minimiza a probabilidade de erros e contribui para a criação de um aplicativo estável e confiável.


### Executando os Testes:

Para executar os testes:

* **Testes Unitários:** Execute os testes JUnit utilizando o seu ambiente de desenvolvimento (ex: Android Studio).
* **Testes Instrumentais:** Execute os testes instrumentais com o Android Studio, utilizando o menu "Run" -> "Run 'Todos os Testes Instrumentais'".


## Melhorias Futuras

O desenvolvimento futuro se concentrará em:

* **Integração com Sistemas Automotivos:** Conectar o aplicativo ao sistema de áudio de um veículo para controle direto.
* **Recursos Avançados de Equalização:** Explorar algoritmos e opções de equalização mais sofisticados.
* **Recursos Adicionais:** Implementar recursos como predefinições, visualizações, sincronização entre dispositivos, tela para escolha de quais contatos importar, modo escuro, controle de acesso e recursos de interação social.

