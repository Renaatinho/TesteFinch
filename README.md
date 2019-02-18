# Bem-vindo !

Olá, este projeto tem como finalidade acessar o site https://globoesporte.globo.com/ e coletar informações sobre as próximas
partidas de futebol e basquete.

# Configurações

Após baixar o projeto será necessário configurar o ambiente para que seja possível  executar o Robô.

## Banco de dados

Este projeto foi desenvolvido para utilização com **MySQL >= 5.5** ou **MariaDB >= 10**

Será necessário criar uma nova base de dados, você poderá informar o nome desejado para a base de dados, porém se atente para que o **Charset** seja **utf8** e a **Collation** seja **utf8_general_ci**

Execute o script **banco.sql** localizado na raiz do projeto no banco de dados criado.

```
CREATE TABLE `partidas` (
`id` INT NOT NULL,
`data_partida` DATETIME NOT NULL,
`campeonato` VARCHAR(45) NOT NULL,
`time_mandante` VARCHAR(45) NOT NULL,
`placar_time_mandante` INT UNSIGNED NULL,
`time_visitante` VARCHAR(45) NOT NULL,
`placar_time_visitante` INT UNSIGNED NULL,
PRIMARY KEY (`id`));
```

## Configurações no Projeto

Edite o arquivo **config.properties** localizado na raiz do projeto, pois nele vamos realizar as configurações do banco de dados.

|Variável|Descrição|
|----------------|------------------------------------------------------------|
|dbhost|endereço ip ou host que o banco de dados está localizado - **padrão: localhost**|
|dbport|porta do servidor de banco de dados - **padrão: 3306**|
|dbname|nome do banco de dados que foi criado e executado o script da criação da tabela.|
|dbuser|nome do usuário do banco de dados que o robô deverá utilizar para acessar o banco (lembrando que é necessário ter permissão de insert/update na tabela de partidas) - **padrão: root**|
|dbpassword|senha de acesso ao banco de dados do usuário informado.|
