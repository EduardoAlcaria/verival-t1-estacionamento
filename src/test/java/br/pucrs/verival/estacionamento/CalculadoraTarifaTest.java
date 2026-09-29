package br.pucrs.verival.estacionamento;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculadoraTarifaTest {

    private final CalculadoraTarifa calculadora = new CalculadoraTarifa();

    @ParameterizedTest(name = "{index} => {4}")
    @MethodSource("casosValidos")
    void calcularValorTarifa(LocalDateTime entrada, LocalDateTime saida, boolean vip, double esperado, String descricao) {
        assertEquals(esperado, calculadora.calcular(entrada, saida, vip), 0.0001, descricao);
    }

    static Stream<Arguments> casosValidos() {
        return Stream.of(
            Arguments.of(dt(2026, 3, 10, 8, 0), dt(2026, 3, 10, 8, 19), false, 0.0, "CT01 cortesia 19min"),
            Arguments.of(dt(2026, 3, 10, 8, 0), dt(2026, 3, 10, 8, 20), false, 0.0, "CT02 cortesia limite 20min"),
            Arguments.of(dt(2026, 3, 10, 8, 0), dt(2026, 3, 10, 8, 21), false, 15.0, "CT03 21min tarifa fixa"),
            Arguments.of(dt(2026, 3, 10, 8, 0), dt(2026, 3, 10, 8, 59), false, 15.0, "CT04 59min tarifa fixa"),
            Arguments.of(dt(2026, 3, 10, 8, 0), dt(2026, 3, 10, 9, 0), false, 15.0, "CT05 limite 60min tarifa fixa"),
            Arguments.of(dt(2026, 3, 10, 8, 0), dt(2026, 3, 10, 9, 1), false, 20.0, "CT06 61min primeira hora adicional"),
            Arguments.of(dt(2026, 3, 10, 8, 0), dt(2026, 3, 10, 10, 0), false, 20.0, "CT07 limite 120min uma hora adicional"),
            Arguments.of(dt(2026, 3, 10, 8, 0), dt(2026, 3, 10, 10, 1), false, 25.0, "CT08 121min duas horas adicionais"),
            Arguments.of(dt(2026, 3, 10, 8, 0), dt(2026, 3, 10, 12, 0), false, 30.0, "CT09 4h de permanencia"),
            Arguments.of(dt(2026, 3, 10, 8, 0), dt(2026, 3, 10, 8, 15), false, 0.0, "CT10 entrada limite 08:00 valida"),
            Arguments.of(dt(2026, 3, 10, 23, 59), dt(2026, 3, 11, 0, 30), false, 15.0, "CT11 entrada limite 23:59 valida"),
            Arguments.of(dt(2026, 3, 10, 23, 0), dt(2026, 3, 11, 1, 59), false, 25.0, "CT12 saida limite 01:59 valida"),
            Arguments.of(dt(2026, 3, 10, 20, 0), dt(2026, 3, 11, 1, 59), false, 40.0, "CT13 saida 01:59 dia seguinte ainda nao pernoite"),
            Arguments.of(dt(2026, 3, 10, 20, 0), dt(2026, 3, 11, 8, 0), false, 50.0, "CT14 saida limite 08:00 dia seguinte pernoite"),
            Arguments.of(dt(2026, 3, 10, 20, 0), dt(2026, 3, 12, 8, 0), false, 100.0, "CT15 duas pernoites"),
            Arguments.of(dt(2026, 3, 10, 8, 0), dt(2026, 3, 10, 8, 21), true, 7.5, "CT16 vip tarifa fixa com 50% desconto"),
            Arguments.of(dt(2026, 3, 10, 20, 0), dt(2026, 3, 11, 8, 0), true, 25.0, "CT17 vip pernoite com 50% desconto"),
            Arguments.of(dt(2026, 3, 10, 8, 0), dt(2026, 3, 10, 8, 10), true, 0.0, "CT18 vip cortesia continua zero")
        );
    }

    @ParameterizedTest(name = "{index} => {2}")
    @MethodSource("entradasInvalidas")
    void rejeitarEntradaForaDoHorarioPermitido(LocalDateTime entrada, LocalDateTime saida, String descricao) {
        assertThrows(IllegalArgumentException.class, () -> calculadora.calcular(entrada, saida, false), descricao);
    }

    static Stream<Arguments> entradasInvalidas() {
        return Stream.of(
            Arguments.of(dt(2026, 3, 10, 7, 59), dt(2026, 3, 10, 9, 0), "EI01 entrada 07:59 invalida"),
            Arguments.of(dt(2026, 3, 10, 0, 0), dt(2026, 3, 10, 9, 0), "EI02 entrada 00:00 invalida"),
            Arguments.of(dt(2026, 3, 10, 3, 0), dt(2026, 3, 10, 9, 0), "EI03 entrada 03:00 invalida")
        );
    }

    @ParameterizedTest(name = "{index} => {2}")
    @MethodSource("saidasInvalidas")
    void rejeitarSaidaForaDoHorarioPermitido(LocalDateTime entrada, LocalDateTime saida, String descricao) {
        assertThrows(IllegalArgumentException.class, () -> calculadora.calcular(entrada, saida, false), descricao);
    }

    static Stream<Arguments> saidasInvalidas() {
        return Stream.of(
            Arguments.of(dt(2026, 3, 10, 20, 0), dt(2026, 3, 11, 2, 0), "ES01 saida limite 02:00 invalida"),
            Arguments.of(dt(2026, 3, 10, 20, 0), dt(2026, 3, 11, 7, 59), "ES02 saida limite 07:59 invalida"),
            Arguments.of(dt(2026, 3, 10, 20, 0), dt(2026, 3, 11, 5, 0), "ES03 saida 05:00 invalida")
        );
    }

    @Test
    @DisplayName("ET01 saida anterior ou igual a entrada deve ser rejeitada")
    void rejeitarSaidaAnteriorOuIgualEntrada() {
        LocalDateTime entrada = dt(2026, 3, 10, 10, 0);
        assertThrows(IllegalArgumentException.class, () -> calculadora.calcular(entrada, entrada, false));
        assertThrows(IllegalArgumentException.class, () -> calculadora.calcular(entrada, dt(2026, 3, 10, 9, 0), false));
    }

    private static LocalDateTime dt(int ano, int mes, int dia, int hora, int minuto) {
        return LocalDateTime.of(ano, mes, dia, hora, minuto);
    }
}
