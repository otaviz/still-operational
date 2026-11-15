# Still Operational

Jogo educativo planejado em **2D pixel art** sobre logica booleana, com humor e uma instalacao futurista que depende de um zelador humano.

## Etapa 3 — prototipo Java

Esta entrega implementa o **nucleo minimo funcional em console**, nao a interface final do jogo.

- Tres manutencoes sequenciais.
- Escolha entre AND, OR e NOT.
- Validacao de respostas e tentativa novamente em caso de erro.
- Um cartao-token por manutencao concluida nesta execucao.
- Separacao entre regras do jogo e interface de console.

**Limite desta versao:** o progresso e os tokens ficam somente na memoria durante a execucao. Persistencia local e interface 2D ficam para etapas posteriores.

## Executar

Requer **JDK 25** e Maven. No IntelliJ, abra a pasta do projeto como Maven e execute a classe `com.stilloperational.Main`.

Pelo terminal, na raiz do projeto:

```bash
mvn compile
java -cp target/classes com.stilloperational.Main
```

Digite `AND`, `OR`, `NOT` ou `SAIR`.

## Estrutura

- `Main`: entrada e interacao no console.
- `Jogo`: progresso sequencial e tokens.
- `Manutencao`: contexto e verificacao da resposta.
- `Operador`: implementacao dos operadores booleanos.

## Proximos passos

Integrar a logica a uma interface 2D pixel art, implementar salvamento local e expandir as manutencoes. O prototipo visual foi planejado no Figma.
