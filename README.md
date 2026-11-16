# Still Operational

**Still Operational** é um jogo educativo de lógica booleana, planejado como uma experiência **2D em pixel art**, com puzzles, humor e uma ambientação futurista.

## Sobre o jogo

Em uma instalação quase totalmente automatizada, um zelador é o único humano responsável por resolver problemas simples que os sistemas de inteligência artificial não conseguem solucionar. A instalação desperdiça seus recursos com comandos excessivos, enquanto operações básicas de lógica booleana continuam precisando de manutenção.

O jogador deve interpretar cada situação, selecionar o operador lógico adequado e restaurar o funcionamento dos sistemas. Ao concluir manutenções, coleta cartões físicos chamados *tokens*, necessários para avançar no objetivo da história.

O projeto busca ensinar os operadores **AND, OR e NOT** por meio de desafios contextualizados, sem exigir conhecimento prévio de programação.

## Estado atual do desenvolvimento

O projeto está em desenvolvimento. A versão disponível em Java é um **protótipo funcional em console**, usado para validar as regras do jogo antes da implementação da interface 2D em pixel art.

Funcionalidades implementadas:
- Três desafios de manutenção em sequência;
- Seleção dos operadores AND, OR e NOT;
- Verificação de respostas, com possibilidade de tentar novamente;
- Avanço de fase e concessão de um token por manutenção concluída;
- Separação da lógica do jogo e da interação pelo console.

**Limitação atual:** o progresso e os tokens são mantidos apenas durante a execução. O salvamento local e a interface gráfica ainda não foram implementados.

## Como executar

Requisitos: **JDK 25** e **Maven**.

No IntelliJ IDEA, abra o projeto Maven e execute a classe `com.stilloperational.Main`.

Pelo terminal, na raiz do repositório:

```bash
mvn compile
java -cp target/classes com.stilloperational.Main
```

Durante a execução, digite `AND`, `OR`, `NOT` ou `SAIR`.

## Organização do código

- `Main.java` — inicia o programa e recebe as escolhas do jogador.
- `Jogo.java` — controla a sequência de manutenções e a contagem de tokens.
- `Manutencao.java` — representa um desafio e verifica a resposta.
- `Operador.java` — implementa os operadores booleanos.

## Direção do projeto

A proposta final é um jogo **2D em pixel art estilizada**, com exploração de uma instalação futurista, personagens, situações cômicas e puzzles de lógica booleana. A versão em console não substitui essa proposta: ela é a base técnica para o desenvolvimento das próximas versões.

Entre os próximos passos estão a integração com uma interface visual, o salvamento local do progresso e a ampliação das manutenções.
