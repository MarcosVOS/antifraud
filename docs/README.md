# AntiFraud

Este projeto tem o objetivo de mostrar uma sistema com uma solução para fraudes ocorridas em meio à transações bancárias.

## Como rodar localmente

Para rodar localmente usa-se o comando docker compose up --watch.

## Como executar os testes

Comando usado para a execução dos testes: docker compose exec app ./mvnw test

## Expliação da estrutura de pastas

### 1. Arquivos na Raiz do Projeto (Configuração e Ferramentas)

Esses arquivos são fundamentais para configurar o ambiente e as ferramentas de desenvolvimento e execução do projeto:

* **`Dockerfile`**: O "receita" para construir a **imagem Docker** da sua aplicação. Ele especifica como o ambiente de execução da sua aplicação deve ser configurado dentro de um contêiner.
* **`compose.yml`**: Define como os **serviços Docker** do projeto (como a aplicação principal e, potencialmente, um banco de dados) são orquestrados e interagem entre si em um ambiente local ou de desenvolvimento.
* **`default.env`**: Contém **variáveis de ambiente padrão** para a aplicação. É útil para configurar credenciais de banco de dados, portas e outras configurações que podem mudar entre ambientes.
* **`mvnw` e `mvnw.cmd`**: São os scripts do **Maven Wrapper**. Eles permitem que você execute comandos Maven (ex: compilar, testar) sem precisar ter o Maven instalado globalmente na sua máquina, garantindo que a versão correta do Maven seja usada por todos os desenvolvedores.
* **`pom.xml`**: O **Project Object Model** do Maven. Este é o arquivo central de configuração do projeto, onde são declaradas todas as **dependências** (bibliotecas utilizadas), plugins e outras configurações de build.
* **`requests/`**: Esta pasta é para **requisições HTTP** de teste, muitas vezes usadas com extensões de IDE para testar a API.
    * `└── users/`: Subpasta para organizar requisições relacionadas a usuários.
        * `└── users.http`: Um arquivo contendo exemplos de requisições HTTP para os endpoints da API de usuários.

### 2. Pasta `src` (Onde o Código-Fonte Reside)

A pasta `src` é o coração do projeto, contendo todo o código-fonte da aplicação e seus respectivos testes. Ela é dividida em `main` (código da aplicação) e `test` (código dos testes).

#### 2.1. `src/main` (Código Principal da Aplicação)

Aqui você encontrará a lógica de negócios e os componentes que fazem o sistema antifraude funcionar:

* **`java/`**: Contém todo o código-fonte Java.
    * `└── com/bradesco/antifraud/`: Esta é a estrutura de pacotes Java da aplicação, seguindo a convenção de nomeação reversa de domínio.
        * **`AntiFraudSystemApplication.java`**: A classe principal que inicializa e executa a aplicação Spring Boot.
        * `├── config/`: Classes de **configuração** da aplicação (ex: segurança, configurações de beans).
        * `├── controller/`: Contém os **controladores REST** (`CustomerController.java`, `HealthCheckController.java`), que são responsáveis por receber as requisições HTTP e roteá-las para os serviços apropriados.
        * `├── dto/`: **Data Transfer Objects (DTOs)**, classes simples para encapsular e transferir dados entre camadas da aplicação ou para o cliente da API.
        * `├── exception/`: Classes para lidar com **exceções** e erros da aplicação (`GlobalExceptionHandler.java`), garantindo respostas de erro padronizadas.
        * `├── mapper/`: Pode conter interfaces ou classes para **mapear objetos** de um tipo para outro (ex: DTO para entidade de domínio).
        * `├── model/`: Contém as classes de **modelo de domínio ou entidades** (`Address.java`, `Customer.java`), que representam as estruturas de dados e a lógica de negócios essencial.
        * `├── repository/`: Interfaces ou classes para **interagir com o banco de dados** (`CustomerRepository.java`), abstraindo as operações de persistência de dados.
        * `├── security/`: Classes relacionadas à **segurança** da aplicação (ex: autenticação, autorização).
        * `└── service/`: Contém as classes de **serviço** (`CustomerService.java`), que implementam a lógica de negócios principal da aplicação, orquestrando operações entre controladores e repositórios.
* **`resources/`**: Contém recursos estáticos e arquivos de configuração para a aplicação.
    * `├── application.properties`: O arquivo principal de **configuração do Spring Boot**, onde propriedades de servidor, banco de dados e outros são definidos.
    * `├── static/`: Para **recursos estáticos** que podem ser servidos diretamente pelo aplicativo (ex: arquivos HTML, CSS, JavaScript, imagens).
    * `└── templates/`: Usado para **templates de view** (se a aplicação renderizar páginas HTML dinamicamente no servidor, usando motores como Thymeleaf).

#### 2.2. `src/test` (Código dos Testes Automatizados)

Esta pasta contém todos os testes automatizados do projeto, seguindo a mesma estrutura de pacotes da pasta `src/main` para facilitar a localização dos testes correspondentes.

* **`java/`**: Contém o código-fonte dos testes Java.
    * `└── com/bradesco/antifraud/`: Estrutura de pacotes para os testes.
        * `├── AntiFraudSystemApplicationTests.java`: Um teste básico que verifica se o contexto da aplicação Spring Boot carrega corretamente.
        * `├── controller/`: Contém os **testes para os controladores** (`CustomerControllerTest.java`, `HealthCheckControllerTest.java`), que simulam requisições HTTP para validar o comportamento da API.
        * `└── service/`: Contém os **testes para as classes de serviço** (`CustomerServiceTest.java`), focando na validação da lógica de negócios.
* **`resources/`**: Contém recursos específicos para o ambiente de teste.
    * `└── application.properties`: Um arquivo de propriedades específico para testes, que pode sobrescrever configurações do `main` para o ambiente de teste.

## Fluxo de CI/CD

Para o projeto, o fluxo de CI/CD garante que cada alteração no código passe por um caminho automatizado de validação e entrega, do desenvolvimento à produção. A ideia é ter um sistema antifraude sempre atualizado, testado e pronto para uso.
