# Equalizador de Áudio para Carro - Aplicativo Android

**Um aplicativo Android personalizável para equalização de áudio precisa.**

## Visão Geral

Este projeto é um aplicativo Android nativo desenvolvido como parte de um curso de pós-graduação em Sistemas Embarcados (foco em Desenvolvimento Android). Embora uma versão futura vise a integração direta com sistemas de áudio automotivos, a versão atual fornece uma experiência independente em um smartphone Android, permitindo que os usuários criem e gerenciem perfis de equalização de áudio personalizados. Isso permite o teste e desenvolvimento da funcionalidade principal antes da integração com o sistema do veículo.

O aplicativo aborda o problema de equalizadores de áudio embarcados limitados ou mal projetados. Muitos veículos oferecem controle insuficiente, deixando os usuários frustrados em suas tentativas de otimizar a experiência auditiva. Nosso aplicativo oferece uma solução flexível e intuitiva.

## Recursos Principais

* **Gerenciamento de Usuários:**
    * **Seleção de Perfil:** Alterne facilmente entre perfis de usuário existentes.
    * **Criação/Edição de Usuário:** Adicione, edite (incluindo nome e ícone do perfil) e exclua perfis de usuário.
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
* **Banco de Dados:** Room Persistence Library
* **Tratamento do Modo Avião:** Broadcast Receivers
* **Testes:** []
* **Injeção de Dependências:** []


## Equipe

* Alisson Freitas
* Eduardo Perez Uanús
* João Gabriel A. Gomes Alves
* Rayanne da Silva Andrade


## Melhorias Futuras

O desenvolvimento futuro se concentrará em:

* **Integração com Sistemas Automotivos:** Conectar o aplicativo ao sistema de áudio de um veículo para controle direto.
* **Recursos Avançados de Equalização:** Explorar algoritmos e opções de equalização mais sofisticados.
* **Recursos Adicionais:** Implementar recursos como predefinições, visualizações, etc.

## Iniciando

[]

## Imagens do Aplicativo

```markdown
![Tela Principal](images/screensapp.png)
![Tela Principal](images/userPag.png)
```markdown



