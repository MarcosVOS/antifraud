# 🛡️AntiFraud🛡️

Este projeto tem o objetivo de mostrar uma sistema com uma solução para fraudes ocorridas em meio à transações bancárias.

## 🚀 Como Rodar Localmente

Para colocar o projeto no ar em sua máquina local e aproveitar a **produtividade máxima** com o **recarregamento automático (hot reload)** das suas alterações no código, basta usar o seguinte comando:
**docker compose up --watch**

## ✅ Como Executar os Testes
A qualidade é fundamental em um sistema antifraude! Para garantir que tudo está funcionando como esperado e que as novas funcionalidades não quebraram nada, execute os testes automatizados com este comando:
**docker compose exec app ./mvnw test**

## 📁 Explicação da Estrutura de Pastas
A organização deste projeto segue uma estrutura de pastas clara e padronizada, típica de aplicações Java Spring Boot que utilizam Maven para gerenciamento de dependências e build, e Docker para conteinerização. Essa estrutura é projetada para promover a modularidade, clareza e facilidade de manutenção.

### 1. Arquivos na Raiz do Projeto (Configuração e Ferramentas Essenciais)
São os alicerces do projeto, configurando o ambiente e as principais ferramentas de desenvolvimento e execução:

Dockerfile: 🐳 É a "receita" para construir a imagem Docker da sua aplicação. Ele especifica como o ambiente de execução deve ser configurado dentro de um contêiner, garantindo consistência em qualquer lugar.
compose.yml: Define como os serviços Docker do projeto (como a aplicação principal e, talvez, um banco de dados) são orquestrados e como eles interagem entre si em seu ambiente local.
default.env: Contém variáveis de ambiente padrão para a aplicação. É super útil para configurar dados sensíveis ou específicos do ambiente (como credenciais de banco de dados, portas), sem precisar alterar o código-fonte.
mvnw e mvnw.cmd: São os scripts do Maven Wrapper. Eles permitem que qualquer pessoa execute comandos Maven (ex: compilar, testar) sem precisar ter o Maven instalado globalmente na sua máquina. Isso garante que a versão correta do Maven seja sempre usada por todos os desenvolvedores! 🤝
pom.xml: O Project Object Model do Maven. Este é o arquivo central de configuração do seu projeto Maven, onde são declaradas todas as dependências (as bibliotecas que o projeto usa), plugins e outras configurações de build.
requests/: Esta pasta é para requisições HTTP de teste, frequentemente usadas com extensões de IDE para testar a API de forma rápida e eficiente.
└── users/: Subpasta para organizar requisições relacionadas especificamente a usuários.
└── users.http: Um arquivo contendo exemplos de requisições HTTP para os endpoints da API de usuários.

## 2. Pasta src (Onde o Código-Fonte Reside)
A pasta src é o coração do projeto, contendo todo o código-fonte da aplicação e seus respectivos testes. Ela é dividida logicamente em main (código da aplicação) e test (código dos testes).

### 2.1. src/main (Código Principal da Aplicação)
Aqui você encontrará a lógica de negócios e os componentes que fazem o seu sistema antifraude funcionar:

java/: Contém todo o código-fonte Java.
└── com/bradesco/antifraud/: Esta é a estrutura de pacotes Java da aplicação, seguindo a convenção de nomeação reversa de domínio (com.bradesco.antifraud).
AntiFraudSystemApplication.java: 🚀 A classe principal que inicializa e executa a aplicação Spring Boot.
├── config/: Classes de configuração da aplicação (ex: segurança, configurações de beans).
├── controller/: Contém os controladores REST (CustomerController.java, HealthCheckController.java), que são os "portões" da sua API. Eles recebem as requisições HTTP e as encaminham para os serviços apropriados.
├── dto/: Data Transfer Objects (DTOs). São classes simples usadas para encapsular e transferir dados entre camadas da aplicação ou para o cliente da API, geralmente para serialização/desserialização JSON.
├── exception/: Classes para lidar com exceções e erros na sua aplicação (GlobalExceptionHandler.java), garantindo que a API retorne respostas de erro padronizadas e úteis.
├── mapper/: Pode conter lógica para converter objetos de um tipo para outro (ex: de um DTO para uma entidade de banco de dados), frequentemente usando bibliotecas de mapeamento.
├── model/: Contém as classes de modelo de domínio ou entidades (Address.java, Customer.java), que representam as estruturas de dados e a lógica de negócios essencial do seu sistema.
├── repository/: Interfaces ou classes que lidam com a interação com o banco de dados (CustomerRepository.java), abstraindo as operações de persistência de dados (salvar, buscar, etc.).
├── security/: Classes relacionadas à segurança da aplicação (ex: autenticação, autorização de usuários).
└── service/: Contém as classes de serviço (CustomerService.java), que implementam a lógica de negócios principal da aplicação, orquestrando as operações entre os controladores e os repositórios.
resources/: Inclui arquivos de configuração e recursos estáticos para a aplicação.
├── application.properties: O principal arquivo de configuração do Spring Boot, onde você define propriedades como portas do servidor, configurações de banco de dados, níveis de log e muito mais.
├── static/: Para recursos estáticos (HTML, CSS, JavaScript, imagens) que podem ser servidos diretamente pelo aplicativo (ex: se houver um pequeno frontend).
└── templates/: Usado para templates de view (se a aplicação renderizar páginas HTML dinamicamente no servidor, usando motores como Thymeleaf).

### 2.2. src/test (Código dos Testes Automatizados)
Esta pasta contém todos os testes automatizados do projeto, espelhando a estrutura de pacotes de src/main para facilitar a localização dos testes correspondentes. É aqui que garantimos a confiabilidade do sistema! 🧪

java/: Contém o código-fonte dos testes Java.
└── com/bradesco/antifraud/: Estrutura de pacotes para os testes.
├── AntiFraudSystemApplicationTests.java: Um teste básico que verifica se o contexto da aplicação Spring Boot carrega corretamente, garantindo que a aplicação pode ser iniciada.
├── controller/: Contém os testes para seus controladores (CustomerControllerTest.java, HealthCheckControllerTest.java), simulando requisições HTTP para validar o comportamento da API.
└── service/: Contém os testes para suas classes de serviço (CustomerServiceTest.java), focando na validação da lógica de negócios principal.
resources/: Contém recursos específicos para o ambiente de teste.
└── application.properties: Um arquivo de propriedades específico para testes, que pode sobrescrever configurações do main para o ambiente de teste (ex: usar um banco de dados em memória para testes).

## 🔁 Fluxo de CI/CD (Integração e Entrega Contínua)
O Fluxo de CI/CD para o "AntiFraudSystem" é uma automação essencial que garante que cada alteração no código seja validada e entregue de forma eficiente, do desenvolvimento à produção. A ideia é ter um sistema antifraude sempre atualizado, testado e pronto para uso! 🚀✨

### 1. Integração Contínua (CI): Qualidade e Verificação Automática 🚦
Esta fase foca em integrar o código frequentemente e detectar problemas rapidamente:

Commit do Código: 🧑‍💻 Um desenvolvedor finaliza sua funcionalidade ou correção e envia (git push) suas alterações para o repositório Git.
Gatilho Automático: ⚡️ Cada push (ou a abertura de um Pull Request/Merge Request) aciona automaticamente o pipeline de CI no servidor (ex: GitLab CI/CD, GitHub Actions).
Build & Testes: 🏗️ O servidor de CI, utilizando o mvnw do projeto, realiza:
Compilação: Garante que todo o código (src/main/java) seja compilado sem erros.
Execução de Testes: Roda todos os testes automatizados (src/test/java), incluindo testes unitários e de integração (CustomerControllerTest, CustomerServiceTest). Se qualquer teste falhar, o pipeline é interrompido imediatamente, e o desenvolvedor é alertado para corrigir.
Análise de Qualidade (Opcional): 🔍 Ferramentas podem analisar o código em busca de bugs, vulnerabilidades de segurança e padrões de código (ex: SonarQube).
Criação do Artefato: 📦 Se tudo passar, o Maven empacota a aplicação, gerando o arquivo JAR executável (antifraud-system.jar).
Construção da Imagem Docker: 🏗️ Usando o Dockerfile, uma imagem Docker da aplicação é criada, empacotando o JAR e tudo o que ela precisa para rodar em um contêiner isolado.
Push para Registro: ⬆️ A imagem Docker é então enviada para um registro de contêineres (ex: Docker Hub, GitLab Container Registry), tornando-a disponível para implantação em qualquer ambiente.
Resultado da CI: Uma imagem Docker do "AntiFraudSystem" que foi exaustivamente compilada e testada, pronta para as próximas etapas de entrega. 🌟

### 2. Entrega Contínua (CD): Do Teste à Produção 🚀
Após a validação bem-sucedida pela CI, o CD se encarrega de levar a aplicação aos ambientes de teste e, finalmente, aos usuários:

Implantação em Homologação: 🧪 A imagem Docker recém-criada e validada é automaticamente puxada e implantada em um ambiente de homologação (staging ou QA). Este ambiente é configurado para espelhar a produção, permitindo testes realistas.
Testes Mais Amplos: 🌐 Nesta fase, podem ser realizados testes adicionais e mais aprofundados, como testes de aceitação do usuário (UAT), testes de performance e testes de segurança, para uma validação completa do sistema em um ambiente próximo ao real.
Aprovação para Produção (Entrega Contínua): ✅ Se o projeto optar pela Entrega Contínua, após a validação em homologação, uma aprovação manual é necessária (geralmente um "clique" em um botão no sistema de CI/CD) para iniciar a implantação em produção.
Implantação em Produção (Implantação Contínua - se for o caso): 🚀 Se o projeto estiver configurado para Implantação Contínua, a implantação em produção acontece automaticamente assim que todas as etapas e testes anteriores forem concluídos com sucesso, sem intervenção humana. O sistema antifraude está agora ativo para os clientes!
Monitoramento: 👀 Uma vez em produção, a aplicação é continuamente monitorada para garantir seu funcionamento ideal, desempenho e para detectar rapidamente quaisquer anomalias ou problemas.
