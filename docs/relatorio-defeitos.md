# Trabalho 1 — Verificação e Validação de Software
## Relatório de Defeitos — Primeira Versão da Implementação

Classe analisada: `br.pucrs.verival.estacionamento.CalculadoraTarifa`
Código da primeira versão preservado em: `docs/v1/CalculadoraTarifa.java`

---

## 1. Resultado da execução sobre a primeira versão

| Métrica | Valor |
|---|---|
| Casos de teste executados | 36 |
| Casos aprovados | 21 |
| Casos reprovados | 15 |

Casos reprovados: CT02, CT03, CT05, CT06, CT07, CT08, CT13, CT14, CT16, CT18, CT20, CT22, CT26, CT27, CT36.

As 15 falhas foram rastreadas até **5 defeitos distintos** no código.

---

## 2. Defeitos encontrados

### Defeito 01 — Cortesia de 20 minutos tratada como intervalo exclusivo

**a) Falha observada**
Uma permanência de exatamente 20 minutos era cobrada como tarifa cheia da primeira hora
(R$ 15,00, ou R$ 7,50 para VIP) em vez de ser isenta.

| Caso | Entrada | Saída | VIP | Esperado | Obtido |
|---|---|---|---|---|---|
| CT13 | 10/03/2026 10:00 | 10/03/2026 10:20 | não | R$ 0,00 | R$ 15,00 |
| CT14 | 10/03/2026 10:00 | 10/03/2026 10:20 | sim | R$ 0,00 | R$ 7,50 |

**b) Caso de teste que detectou**
CT13 e CT14 — valor limite superior da partição PD1 (cortesia).

**c) Correção**
O enunciado concede 20 minutos de cortesia, logo o limite pertence à partição isenta.
A comparação estrita foi trocada por comparação inclusiva em `calcularPorHora`:

```java
if (minutos < MINUTOS_CORTESIA)      // antes
if (minutos <= MINUTOS_CORTESIA)     // depois
```

---

### Defeito 02 — Limite da primeira hora tratado como intervalo exclusivo

**a) Falha observada**
Uma permanência de exatamente 60 minutos era classificada como estadia com hora
adicional, sendo cobrada R$ 20,00 em vez dos R$ 15,00 fixos.

| Caso | Entrada | Saída | VIP | Esperado | Obtido |
|---|---|---|---|---|---|
| CT02 | 10/03/2026 08:00 | 10/03/2026 09:00 | não | R$ 15,00 | R$ 20,00 |
| CT16 | 10/03/2026 10:00 | 10/03/2026 11:00 | não | R$ 15,00 | R$ 20,00 |
| CT18 | 10/03/2026 10:00 | 10/03/2026 11:00 | sim | R$ 7,50 | R$ 10,00 |

**b) Caso de teste que detectou**
CT16 e CT18 — valor limite superior da partição PD2. CT02 confirmou o mesmo defeito
combinado com o limite inferior do horário de entrada.

**c) Correção**
O enunciado diz "até 1 hora (**inclusive**)". A comparação estrita foi trocada por
comparação inclusiva em `calcularPorHora`:

```java
if (minutos < 60)                          // antes
if (minutos <= MINUTOS_PRIMEIRA_HORA)      // depois
```

---

### Defeito 03 — Intervalo de hora adicional cobrado antes de ser completado

**a) Falha observada**
O cálculo das horas adicionais somava um intervalo a mais sempre que a duração era um
múltiplo exato de 60 minutos, porque a fórmula usava divisão inteira seguida de `+ 1`
em vez de arredondamento para cima.

| Caso | Entrada | Saída | Duração | Esperado | Obtido |
|---|---|---|---|---|---|
| CT20 | 10/03/2026 10:00 | 10/03/2026 12:00 | 120 min | R$ 20,00 | R$ 25,00 |
| CT22 | 10/03/2026 10:00 | 10/03/2026 13:00 | 180 min | R$ 25,00 | R$ 30,00 |

**b) Caso de teste que detectou**
CT20 e CT22 — valores limite superiores das partições PD3 e PD4.

**c) Correção**
O enunciado diz "a cada intervalo de 1 hora (**inclusive**)", ou seja, o intervalo só é
cobrado quando de fato se completa. A fórmula foi trocada por um arredondamento para
cima do excedente:

```java
long horasAdicionais = (minutos - 60) / 60 + 1;                                    // antes
long intervalosAdicionais = (excedente + MINUTOS_PRIMEIRA_HORA - 1)
                            / MINUTOS_PRIMEIRA_HORA;                               // depois
```

---

### Defeito 04 — Pernoite detectado por simples mudança de data de calendário

**a) Falha observada**
O código considerava pernoite qualquer estadia em que a data de saída fosse posterior à
data de entrada. Como o estacionamento opera até as 02:00 da manhã, toda saída após a
meia-noite era indevidamente convertida em pernoite de R$ 50,00, mesmo com poucas horas
de permanência. O mesmo defeito causava o erro oposto na saída às 08:00 do dia seguinte,
cobrando pernoite quando a regra ainda manda aplicar a tarifa horária.

| Caso | Entrada | Saída | Duração | Esperado | Obtido |
|---|---|---|---|---|---|
| CT03 | 10/03/2026 23:59 | 11/03/2026 00:59 | 60 min | R$ 15,00 | R$ 50,00 |
| CT05 | 10/03/2026 22:00 | 11/03/2026 01:59 | 239 min | R$ 30,00 | R$ 50,00 |
| CT08 | 10/03/2026 20:00 | 11/03/2026 08:00 | 720 min | R$ 70,00 | R$ 50,00 |
| CT26 | 10/03/2026 23:00 | 11/03/2026 01:00 | 120 min | R$ 20,00 | R$ 50,00 |
| CT27 | 10/03/2026 23:00 | 11/03/2026 01:00 | 120 min (VIP) | R$ 10,00 | R$ 25,00 |
| CT36 | 31/01/2026 22:00 | 01/02/2026 01:00 | 180 min | R$ 25,00 | R$ 50,00 |

Este foi o defeito de maior impacto: sozinho respondeu por 6 das 15 falhas.

**b) Caso de teste que detectou**
CT26 (estadia curta atravessando a meia-noite) e CT08 (valor limite exato das 08:00 do
dia seguinte).

**c) Correção**
A condição de pernoite passou a comparar o instante de saída com o limiar explícito do
enunciado — 08:00 da manhã do dia seguinte à entrada — em vez de comparar apenas datas:

```java
return ticket.getSaida().toLocalDate()
        .isAfter(ticket.getEntrada().toLocalDate());                   // antes

LocalDateTime limite = ticket.getEntrada().toLocalDate()
        .plusDays(1).atTime(HORA_ABERTURA, 0);
return ticket.getSaida().isAfter(limite);                              // depois
```

---

### Defeito 05 — Ausência de validação da janela de saída

**a) Falha observada**
A validação cobria apenas o horário de entrada e a ordem cronológica das datas. Saídas
entre 02:00 e 07:59, proibidas pelo enunciado, eram aceitas e tarifadas normalmente em
vez de rejeitadas.

| Caso | Entrada | Saída | Esperado | Obtido |
|---|---|---|---|---|
| CT06 | 10/03/2026 22:00 | 11/03/2026 02:00 | `EstacionamentoException` | R$ 50,00, sem exceção |
| CT07 | 10/03/2026 22:00 | 11/03/2026 07:59 | `EstacionamentoException` | R$ 50,00, sem exceção |

**b) Caso de teste que detectou**
CT06 e CT07 — valores limite inferior e superior da partição inválida PS2.

**c) Correção**
Foi adicionada a verificação da janela de saída no método `validar`:

```java
int horaSaida = ticket.getSaida().getHour();
if (horaSaida >= HORA_INICIO_BLOQUEIO_SAIDA && horaSaida <= HORA_FIM_BLOQUEIO_SAIDA) {
    throw new EstacionamentoException(
            "Saida nao permitida das 02:00 as 07:59, recebido " + ticket.getSaida());
}
```

---

## 3. Resultado após as correções

| Métrica | Primeira versão | Versão corrigida |
|---|---|---|
| Casos executados | 36 | 36 |
| Casos aprovados | 21 | 36 |
| Casos reprovados | 15 | 0 |

Todos os 36 casos de teste projetados passam na versão corrigida.

---

## 4. Observações sobre o projeto dos testes

Os 5 defeitos foram detectados exclusivamente por casos de **valor limite**. Nenhum deles
teria sido revelado por casos escolhidos no meio das partições de equivalência:

| Defeito | Limite que o revelou |
|---|---|
| 01 | 20 min — fronteira cortesia / tarifa fixa |
| 02 | 60 min — fronteira tarifa fixa / tarifa escalonada |
| 03 | 120 e 180 min — fronteiras entre intervalos de hora adicional |
| 04 | 08:00 do dia seguinte — fronteira tarifa horária / pernoite |
| 05 | 02:00 e 07:59 — fronteiras da janela proibida de saída |

Cabe registrar uma consequência contraintuitiva das regras do enunciado, confirmada por
CT08: uma estadia de 20:00 às 08:00 do dia seguinte (12 horas) custa R$ 70,00 pela tarifa
horária, enquanto sair um minuto depois, às 08:01, custa R$ 50,00 como pernoite. Sair mais
tarde sai mais barato. Não é um defeito da implementação — é o que as regras determinam —
mas é um ponto que merece confirmação junto ao cliente.
