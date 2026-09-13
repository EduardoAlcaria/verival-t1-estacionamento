# Trabalho 1 — Verificação e Validação de Software
## Projeto dos Casos de Teste — Cálculo da Tarifa de Estacionamento

Classe sob teste: `br.pucrs.verival.estacionamento.CalculadoraTarifa`
Método sob teste: `double calcular(Ticket ticket)`

---

## 1. Premissas de interpretação do enunciado

O enunciado deixa pontos ambíguos. As decisões abaixo foram adotadas e são a base
do oráculo de todos os casos de teste.

| # | Ponto ambíguo | Decisão adotada |
|---|---|---|
| A1 | "20 minutos de cortesia" — 20 min exatos cobram? | Não. Cortesia é inclusiva: duração ≤ 20 min ⇒ R$ 0,00 |
| A2 | "Até 1 hora (inclusive) R$ 15,00" | Duração de 21 a 60 min ⇒ R$ 15,00 |
| A3 | "+ R$ 5,00 a cada intervalo de 1 hora (inclusive)" | `15 + 5 × teto((min − 60) / 60)`. Logo 120 min = R$ 20,00 e 121 min = R$ 25,00 |
| A4 | "saia após as 08:00 do dia seguinte" — 08:00 exato é pernoite? | Não. Pernoite exige saída **estritamente posterior** a 08:00 do dia seguinte à entrada |
| A5 | "R$ 50,00 por pernoite" — como contar pernoites? | Nº de pernoites = nº de dias de calendário entre a data de entrada e a data de saída |
| A6 | Pernoite soma ou substitui a tarifa horária? | Substitui ("a tarifa é convertida para pernoite") |
| A7 | VIP incide sobre pernoite? | Sim. O desconto de 50% é o último passo, sobre o valor final |
| A8 | O que fazer com entrada/saída fora das janelas permitidas? | Lançar `EstacionamentoException` |
| A9 | Saída igual à entrada é válida? | Sim, duração 0 ⇒ R$ 0,00. Apenas saída **anterior** à entrada é inválida |

## 2. Variáveis de entrada

| Variável | Domínio |
|---|---|
| data/hora de entrada | dia, mês, ano, hora, minuto |
| data/hora de saída | dia, mês, ano, hora, minuto |
| vip | `true` / `false` |

## 3. Particionamento de equivalência

### 3.1 Horário de entrada (regra: entrada permitida das 08:00 às 23:59)

| Partição | Faixa | Classe |
|---|---|---|
| PE1 | 00:00 – 07:59 | inválida |
| PE2 | 08:00 – 23:59 | válida |

Valores limite: **07:59**, **08:00**, **23:59**, **00:00**

### 3.2 Horário de saída (regra: saída proibida das 02:00 às 07:59)

| Partição | Faixa | Classe |
|---|---|---|
| PS1 | 08:00 – 01:59 (atravessa a meia-noite) | válida |
| PS2 | 02:00 – 07:59 | inválida |

Valores limite: **01:59**, **02:00**, **07:59**, **08:00**

### 3.3 Duração da permanência (estadias sem pernoite)

| Partição | Faixa (min) | Valor esperado |
|---|---|---|
| PD1 | 0 – 20 | R$ 0,00 |
| PD2 | 21 – 60 | R$ 15,00 |
| PD3 | 61 – 120 | R$ 20,00 |
| PD4 | 121 – 180 | R$ 25,00 |
| PD5 | 181 – 240 | R$ 30,00 |

Valores limite: **0, 20, 21, 60, 61, 120, 121, 180, 181**

### 3.4 Pernoite

| Partição | Condição | Valor esperado |
|---|---|---|
| PN1 | saída ≤ 08:00 do dia seguinte | tarifa horária (não é pernoite) |
| PN2 | 1 pernoite | R$ 50,00 |
| PN3 | 2 pernoites | R$ 100,00 |
| PN4 | 3 pernoites | R$ 150,00 |

Valor limite: saída em **d+1 08:00** (não pernoite) × **d+1 08:01** (pernoite)

### 3.5 Cliente VIP

| Partição | Valor |
|---|---|
| PV1 | `false` — valor integral |
| PV2 | `true` — 50% do valor integral |

### 3.6 Validade da data informada

| Partição | Classe |
|---|---|
| PDT1 | data existente no calendário | válida |
| PDT2 | data inexistente (30/02, mês 13, dia 0) | inválida |
| PDT3 | saída anterior à entrada | inválida |

---

## 4. Tabela de casos de teste

Datas no formato `dd/mm/aaaa hh:mm`.

### 4.1 Validação das janelas de operação e das datas

| Caso | Entrada | Saída | VIP | Partições | Resultado esperado |
|---|---|---|---|---|---|
| CT01 | 10/03/2026 07:59 | 10/03/2026 10:00 | não | PE1 (limite inf.) | `EstacionamentoException` |
| CT02 | 10/03/2026 08:00 | 10/03/2026 09:00 | não | PE2 (limite inf.), PD2 | R$ 15,00 |
| CT03 | 10/03/2026 23:59 | 11/03/2026 00:59 | não | PE2 (limite sup.), PD2 | R$ 15,00 |
| CT04 | 10/03/2026 00:00 | 10/03/2026 10:00 | não | PE1 | `EstacionamentoException` |
| CT05 | 10/03/2026 22:00 | 11/03/2026 01:59 | não | PS1 (limite sup.), PD5 | R$ 30,00 |
| CT06 | 10/03/2026 22:00 | 11/03/2026 02:00 | não | PS2 (limite inf.) | `EstacionamentoException` |
| CT07 | 10/03/2026 22:00 | 11/03/2026 07:59 | não | PS2 (limite sup.) | `EstacionamentoException` |
| CT08 | 10/03/2026 20:00 | 11/03/2026 08:00 | não | PS1 (limite inf.), PN1 | R$ 70,00 |
| CT09 | 10/03/2026 12:00 | 10/03/2026 11:00 | não | PDT3 | `EstacionamentoException` |
| CT10 | 30/02/2026 10:00 | 30/02/2026 11:00 | não | PDT2 | `EstacionamentoException` |
| CT11 | 10/13/2026 10:00 | 10/13/2026 11:00 | não | PDT2 | `EstacionamentoException` |

### 4.2 Cortesia — PD1

| Caso | Entrada | Saída | VIP | Duração | Resultado esperado |
|---|---|---|---|---|---|
| CT12 | 10/03/2026 10:00 | 10/03/2026 10:00 | não | 0 min (limite inf.) | R$ 0,00 |
| CT13 | 10/03/2026 10:00 | 10/03/2026 10:20 | não | 20 min (limite sup.) | R$ 0,00 |
| CT14 | 10/03/2026 10:00 | 10/03/2026 10:20 | sim | 20 min | R$ 0,00 |

### 4.3 Tarifa fixa — PD2

| Caso | Entrada | Saída | VIP | Duração | Resultado esperado |
|---|---|---|---|---|---|
| CT15 | 10/03/2026 10:00 | 10/03/2026 10:21 | não | 21 min (limite inf.) | R$ 15,00 |
| CT16 | 10/03/2026 10:00 | 10/03/2026 11:00 | não | 60 min (limite sup.) | R$ 15,00 |
| CT17 | 10/03/2026 10:00 | 10/03/2026 10:21 | sim | 21 min | R$ 7,50 |
| CT18 | 10/03/2026 10:00 | 10/03/2026 11:00 | sim | 60 min | R$ 7,50 |

### 4.4 Tarifa escalonada — PD3, PD4, PD5

| Caso | Entrada | Saída | VIP | Duração | Resultado esperado |
|---|---|---|---|---|---|
| CT19 | 10/03/2026 10:00 | 10/03/2026 11:01 | não | 61 min (limite inf. PD3) | R$ 20,00 |
| CT20 | 10/03/2026 10:00 | 10/03/2026 12:00 | não | 120 min (limite sup. PD3) | R$ 20,00 |
| CT21 | 10/03/2026 10:00 | 10/03/2026 12:01 | não | 121 min (limite inf. PD4) | R$ 25,00 |
| CT22 | 10/03/2026 10:00 | 10/03/2026 13:00 | não | 180 min (limite sup. PD4) | R$ 25,00 |
| CT23 | 10/03/2026 10:00 | 10/03/2026 13:01 | não | 181 min (limite inf. PD5) | R$ 30,00 |
| CT24 | 10/03/2026 10:00 | 10/03/2026 11:01 | sim | 61 min | R$ 10,00 |
| CT25 | 10/03/2026 10:00 | 10/03/2026 12:01 | sim | 121 min | R$ 12,50 |

### 4.5 Virada de dia sem pernoite — PN1

| Caso | Entrada | Saída | VIP | Duração | Resultado esperado |
|---|---|---|---|---|---|
| CT26 | 10/03/2026 23:00 | 11/03/2026 01:00 | não | 120 min | R$ 20,00 |
| CT27 | 10/03/2026 23:00 | 11/03/2026 01:00 | sim | 120 min | R$ 10,00 |

### 4.6 Pernoite — PN2, PN3, PN4

| Caso | Entrada | Saída | VIP | Pernoites | Resultado esperado |
|---|---|---|---|---|---|
| CT28 | 10/03/2026 20:00 | 11/03/2026 08:01 | não | 1 (limite) | R$ 50,00 |
| CT29 | 10/03/2026 10:00 | 11/03/2026 09:00 | não | 1 | R$ 50,00 |
| CT30 | 10/03/2026 10:00 | 12/03/2026 09:00 | não | 2 | R$ 100,00 |
| CT31 | 10/03/2026 10:00 | 13/03/2026 09:00 | não | 3 | R$ 150,00 |
| CT32 | 10/03/2026 20:00 | 11/03/2026 08:01 | sim | 1 | R$ 25,00 |
| CT33 | 10/03/2026 10:00 | 12/03/2026 09:00 | sim | 2 | R$ 50,00 |

### 4.7 Viradas de mês e de ano

| Caso | Entrada | Saída | VIP | Observação | Resultado esperado |
|---|---|---|---|---|---|
| CT34 | 31/12/2026 20:00 | 01/01/2027 09:00 | não | virada de ano, 1 pernoite | R$ 50,00 |
| CT35 | 28/02/2028 20:00 | 29/02/2028 09:00 | não | ano bissexto, 1 pernoite | R$ 50,00 |
| CT36 | 31/01/2026 22:00 | 01/02/2026 01:00 | não | virada de mês, 180 min, sem pernoite | R$ 25,00 |

---

## 5. Resumo de cobertura

| Partição | Casos que a cobrem |
|---|---|
| PE1 | CT01, CT04 |
| PE2 | CT02, CT03 e demais |
| PS1 | CT05, CT08 e demais |
| PS2 | CT06, CT07 |
| PD1 | CT12, CT13, CT14 |
| PD2 | CT02, CT03, CT15–CT18 |
| PD3 | CT19, CT20, CT24, CT26, CT27 |
| PD4 | CT21, CT22, CT25, CT36 |
| PD5 | CT05, CT23 |
| PN1 | CT08, CT26, CT27, CT36 |
| PN2 | CT28, CT29, CT32, CT34, CT35 |
| PN3 | CT30, CT33 |
| PN4 | CT31 |
| PV1 | CT01–CT13, CT15, CT16, CT19–CT23, CT26, CT28–CT31, CT34–CT36 |
| PV2 | CT14, CT17, CT18, CT24, CT25, CT27, CT32, CT33 |
| PDT1 | todos os casos válidos |
| PDT2 | CT10, CT11 |
| PDT3 | CT09 |
