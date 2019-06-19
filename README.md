
# DEMO

## Requisitos básicos

Para que a aplicação possa ser executada, são necessárias os seguintes componentes já instalados na máquina:

* [Java JDK 1.8](https://www.oracle.com/technetwork/pt/java/javase/downloads/jdk8-downloads-2133151.html)
* [Docker CE](https://docs.docker.com/install/)

## Sobre a aplicação

A aplicação desenvolvida é uma aplicação baseada no framework [Spring Boot](https://spring.io/projects/spring-boot).  A documentação do framework está disponível [aqui](https://docs.spring.io/spring-boot/docs/2.1.6.RELEASE/reference/html/).

Esta versão usa uma base de dados embarcada do tipo [H2](https://www.h2database.com/html/main.html), mas que pode ser substituída por qualquer base de dados compatível com a plataforma Java, apenas sendo necessária a inclusão do driver JDBC, da inclusão do driver JDBC na lista de dependências da aplicação e da configuração necessária no arquivo `application.properties`.

```properties
spring.datasource.url=jdbc:mysql://localhost/test
spring.datasource.username=dbuser
spring.datasource.password=dbpass
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
```

## Iniciando

A partir de um clone desse repositório, execute o [Maven](https://maven.apache.org/guides/index.html) para gerar uma versão executável da aplicação:

```shell script
./mvnw package
```

Neste processo, as dependências necessárias para a aplicação serão buscadas na Internet (ou em um cache local, caso já esteja configurado), a aplicação será compilada, os testes básicos serão executados, uma versão no formato `jar` será gerada na pasta `target` e uma imagem docker de nome `api-server-image` com etiqueta (tag) `latest` será instalada no ambiente Docker local. 

### Executando a aplicação localmente

Após a conclusão do processo de construção do `Maven`, executar o seguinte comando: 

```shell script
java jar target/demo-0.0.1-SNAPSHOT.jar
```

Após uma breve inicialização, um servidor web estará disponível no http://localhost:8080. 

Uma página com a documentação básica das APIs disponíveis está disponível em http://localhost:8080/swagger-ui.html.  Nesta página, é possível a realização de chamadas nas APIs, e também a geração de comandos `curl` para operacionalização das APIs através de linha de comando.  

Um endpoint para validação do estado da aplicação está disponível em http://localhost:8080/health. Diversas métricas estão disponíveis através do endereço (http://localhost:8080/metrics).  AS métricas específicas de acesso às APIs de TODO estão disponíveis em http://localhost:8080/metrics/TODO_ENDPOINT. 

### Escalando a aplicação via container

Uma imagem Docker é gerada automaticamente no processo de construção da aplicação.  Essa imagem pode ser disponibilizada em containers compatíveis e a aplicação pode ser escalada horizontalmente de maneira rápida.

