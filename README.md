# Trabalho 1 — Verificação e Validação de Software

Cálculo da tarifa de estacionamento de centro comercial, com casos de teste projetados
por particionamento de equivalência e análise de valor limite, implementados em JUnit 5
com testes parametrizados.

## Estrutura

```
pom.xml                                  projeto Maven
run-tests.sh                             execução sem Maven instalado
lib/junit-platform-console-standalone.jar
src/main/java/br/pucrs/verival/estacionamento/
    Ticket.java                          dados do ticket
    CalculadoraTarifa.java               classe sob teste (versão corrigida)
    EstacionamentoException.java
src/test/java/br/pucrs/verival/estacionamento/
    CalculadoraTarifaTest.java           36 casos parametrizados
docs/
    casos-de-teste.pdf                   tabela de casos de teste
    relatorio-defeitos.pdf               relatório dos defeitos da primeira versão
    casos-de-teste.md
    relatorio-defeitos.md
    v1/CalculadoraTarifa.java            primeira versão, com os 5 defeitos
```

## Execução dos testes

Com Maven:

```
mvn test
```

Sem Maven (usa apenas `javac`, `java` e o jar em `lib/`):

```
./run-tests.sh
```

## Resultado

| Versão | Casos | Aprovados | Reprovados |
|---|---|---|---|
| Primeira versão (`docs/v1/`) | 36 | 21 | 15 |
| Versão corrigida (`src/main/`) | 36 | 36 | 0 |

As 15 falhas da primeira versão foram rastreadas até 5 defeitos, todos detectados por
casos de valor limite. Detalhamento em `docs/relatorio-defeitos.pdf`.
