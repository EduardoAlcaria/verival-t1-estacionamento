# Trabalho 1: Calculadora de Tarifa de Estacionamento

Verificacao e Validacao de Software, PUCRS.

Implementacao da classe responsavel pelo calculo do valor a ser pago pelo
ticket de estacionamento de um centro comercial, com casos de teste unitarios
projetados por particionamento em classes de equivalencia e analise de valor
limite.

## Regras de negocio

- Entrada permitida das 08:00 as 23:59.
- Saida proibida das 02:00 as 07:59.
- 20 minutos de cortesia, sem cobranca.
- Ate 1 hora de permanencia: tarifa fixa de R$ 15,00.
- Acima de 1 hora, sem pernoite: incremento de R$ 5,00 a cada hora iniciada.
- Saida a partir das 08:00 do dia seguinte a entrada: pernoite, R$ 50,00 por
  diaria.
- Cliente VIP: 50% de desconto sobre o valor final.

## Estrutura

```
pom.xml
src/main/java/br/pucrs/verival/estacionamento/CalculadoraTarifa.java
src/test/java/br/pucrs/verival/estacionamento/CalculadoraTarifaTest.java
docs/casos_de_teste.tex   fonte latex da tabela de casos de teste
docs/casos_de_teste.pdf
docs/defeitos_encontrados.tex  fonte latex do relatorio de defeitos da primeira versao
docs/defeitos_encontrados.pdf
```

## Como rodar os testes

Com Maven instalado:

```bash
mvn test
```

## Documentacao

A tabela completa de casos de teste (particionamento e valor limite) esta em
`docs/casos_de_teste.pdf`. O relatorio com os defeitos encontrados na
primeira versao da implementacao, incluindo qual caso de teste detectou cada
falha e como foi corrigida, esta em `docs/defeitos_encontrados.pdf`.
