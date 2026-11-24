
# Still Operational

**Still Operational** é um jogo educativo de lógica booleana, planejado como uma experiência **2D em pixel art**, com puzzles, humor e uma ambientação futurista.

## Sobre o jogo

Em uma instalação quase totalmente automatizada, um zelador é o único humano responsável por resolver problemas simples que os sistemas de inteligência artificial não conseguem solucionar. A instalação desperdiça seus recursos com comandos excessivos, enquanto operações básicas de lógica booleana continuam precisando de manutenção.

O jogador deve interpretar cada situação, selecionar o operador lógico adequado e restaurar o funcionamento dos sistemas. Ao concluir manutenções, coleta cartões físicos chamados *tokens*, necessários para avançar no objetivo da história.

O projeto busca ensinar os operadores **AND, OR e NOT** por meio de desafios contextualizados, sem exigir conhecimento prévio de programação.

## Estado atual do desenvolvimento

A versão disponível em Java é um **protótipo funcional em console**, utilizado para validar as regras do jogo antes da implementação da interface 2D em pixel art.

Funcionalidades implementadas:

- Três desafios de manutenção em sequência;
- Seleção dos operadores AND, OR e NOT;
- Verificação de respostas e possibilidade de tentar novamente;
- Avanço de fase e concessão de um token por manutenção concluída;
- Separação da lógica do jogo e da interação pelo console;
- Testes unitários e de integração;
- Documentação das classes e métodos utilizando JavaDoc.

**Limitação atual:** o progresso e os tokens são mantidos apenas durante a execução. O salvamento local e a interface gráfica ainda não foram implementados.

## Manual do desenvolvedor

### Requisitos

- Java JDK 25;
- Apache Maven;
- IntelliJ IDEA (recomendado);
- Git para versionamento.

### Instalação

Clone o repositório:

```bash
git clone https://github.com/otaviz/still-operational.git
```

Abra a pasta clonada no IntelliJ IDEA e sincronize o projeto Maven para instalar as dependências.

### Como executar

No IntelliJ IDEA, localize a classe `Main.java`, dentro do package `com.stilloperational`, e execute seu método `main`.

Também é possível executar pelo terminal, na raiz do projeto, caso o Maven esteja configurado no sistema:

```bash
mvn compile
java -cp target/classes com.stilloperational.Main
```

### Organização do código

- `Main.java` — inicia o programa e recebe as escolhas do jogador.
- `Jogo.java` — controla as manutenções, a progressão e a contagem de tokens.
- `Manutencao.java` — representa um desafio e verifica a resposta.
- `Operador.java` — implementa os operadores booleanos.
- `pom.xml` — configura o projeto Maven e suas dependências.
- `src/test/java` — armazena os testes automatizados.

### Testes automatizados

O projeto utiliza JUnit 5 para validar suas principais funcionalidades.

| Classe de teste | Responsabilidade | Testes |
|---|---|---|
| OperadorTest | Validação de AND, OR e NOT | 3 |
| ManutencaoTest | Validação de respostas | 2 |
| JogoTest | Estado inicial, progressão e tokens | 3 |
| IntegracaoTest | Simulação de uma partida completa | 1 |
| Total | | 9 |

Para executar os testes pelo IntelliJ IDEA, acesse:

Maven > Lifecycle > test

Caso o Maven esteja configurado no terminal:

```bash
mvn test
```

Na execução realizada em 24/11/2026, os nove testes foram aprovados, sem falhas ou erros.

### Documentação do código

As quatro classes principais possuem comentários JavaDoc, descrevendo suas responsabilidades, métodos, parâmetros e retornos.

## Manual do usuário

### Objetivo

O jogador assume o papel de um zelador responsável por realizar a manutenção de uma instalação automatizada.

Para concluir uma manutenção, é necessário interpretar o problema apresentado e escolher o operador lógico adequado.

### Operadores disponíveis

| Comando | Funcionamento |
|---|---|
| AND | Todas as condições precisam ser verdadeiras. |
| OR | Pelo menos uma condição precisa ser verdadeira. |
| NOT | Inverte o valor lógico de uma condição. |
| SAIR | Encerra a partida. |

### Como jogar

1. Execute o programa.
2. Leia o problema de manutenção apresentado no console.
3. Digite o operador lógico que considera correto.
4. Pressione Enter para confirmar a resposta.
5. Caso acerte, receba um token e avance para a próxima manutenção.
6. Caso erre, tente novamente.
7. Conclua todas as manutenções para finalizar a partida.

### Tokens e progressão

Cada manutenção concluída concede um token ao jogador. As fases são disponibilizadas sequencialmente e respostas incorretas não concedem recompensas.

Ao concluir todas as manutenções, o sistema informa que a instalação foi estabilizada.

O progresso é mantido apenas durante a partida. Ao encerrar o programa, uma nova execução começa do início.

## Direção do projeto

A proposta final é um jogo **2D em pixel art estilizada**, com exploração de uma instalação futurista, personagens, situações cômicas e puzzles de lógica booleana.

A versão em console não substitui essa proposta: ela é a base técnica para o desenvolvimento das próximas versões.

Entre os próximos passos estão a integração com uma interface visual, o salvamento local do progresso e a ampliação das manutenções.

## Autor

Otávio Augusto dos Reis Nascente e Costa

Projeto acadêmico — Engenharia de Software II.
