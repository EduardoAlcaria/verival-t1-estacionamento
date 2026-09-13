package br.pucrs.verival.estacionamento;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("CalculadoraTarifa")
class CalculadoraTarifaTest {

    private static final double DELTA = 0.001;

    private CalculadoraTarifa calculadora;

    @BeforeEach
    void setUp() {
        calculadora = new CalculadoraTarifa();
    }

    @Nested
    @DisplayName("Cortesia - particao PD1")
    class Cortesia {

        @ParameterizedTest(name = "{0}: {1} ate {2} vip={3} => R$ {4}")
        @CsvSource({
                "CT12, 2026-03-10T10:00, 2026-03-10T10:00, false, 0.0",
                "CT13, 2026-03-10T10:00, 2026-03-10T10:20, false, 0.0",
                "CT14, 2026-03-10T10:00, 2026-03-10T10:20, true,  0.0"
        })
        void deveAplicarCortesia(String caso, LocalDateTime entrada, LocalDateTime saida,
                                 boolean vip, double esperado) {
            assertEquals(esperado, calculadora.calcular(new Ticket(entrada, saida, vip)), DELTA, caso);
        }
    }

    @Nested
    @DisplayName("Tarifa fixa da primeira hora - particao PD2")
    class TarifaFixa {

        @ParameterizedTest(name = "{0}: {1} ate {2} vip={3} => R$ {4}")
        @CsvSource({
                "CT02, 2026-03-10T08:00, 2026-03-10T09:00, false, 15.0",
                "CT03, 2026-03-10T23:59, 2026-03-11T00:59, false, 15.0",
                "CT15, 2026-03-10T10:00, 2026-03-10T10:21, false, 15.0",
                "CT16, 2026-03-10T10:00, 2026-03-10T11:00, false, 15.0",
                "CT17, 2026-03-10T10:00, 2026-03-10T10:21, true,   7.5",
                "CT18, 2026-03-10T10:00, 2026-03-10T11:00, true,   7.5"
        })
        void deveCobrarValorFixo(String caso, LocalDateTime entrada, LocalDateTime saida,
                                 boolean vip, double esperado) {
            assertEquals(esperado, calculadora.calcular(new Ticket(entrada, saida, vip)), DELTA, caso);
        }
    }

    @Nested
    @DisplayName("Tarifa escalonada por hora adicional - particoes PD3, PD4, PD5")
    class TarifaEscalonada {

        @ParameterizedTest(name = "{0}: {1} ate {2} vip={3} => R$ {4}")
        @CsvSource({
                "CT05, 2026-03-10T22:00, 2026-03-11T01:59, false, 30.0",
                "CT08, 2026-03-10T20:00, 2026-03-11T08:00, false, 70.0",
                "CT19, 2026-03-10T10:00, 2026-03-10T11:01, false, 20.0",
                "CT20, 2026-03-10T10:00, 2026-03-10T12:00, false, 20.0",
                "CT21, 2026-03-10T10:00, 2026-03-10T12:01, false, 25.0",
                "CT22, 2026-03-10T10:00, 2026-03-10T13:00, false, 25.0",
                "CT23, 2026-03-10T10:00, 2026-03-10T13:01, false, 30.0",
                "CT24, 2026-03-10T10:00, 2026-03-10T11:01, true,  10.0",
                "CT25, 2026-03-10T10:00, 2026-03-10T12:01, true,  12.5",
                "CT26, 2026-03-10T23:00, 2026-03-11T01:00, false, 20.0",
                "CT27, 2026-03-10T23:00, 2026-03-11T01:00, true,  10.0",
                "CT36, 2026-01-31T22:00, 2026-02-01T01:00, false, 25.0"
        })
        void deveCobrarHorasAdicionais(String caso, LocalDateTime entrada, LocalDateTime saida,
                                       boolean vip, double esperado) {
            assertEquals(esperado, calculadora.calcular(new Ticket(entrada, saida, vip)), DELTA, caso);
        }
    }

    @Nested
    @DisplayName("Pernoite - particoes PN2, PN3, PN4")
    class Pernoite {

        @ParameterizedTest(name = "{0}: {1} ate {2} vip={3} => R$ {4}")
        @CsvSource({
                "CT28, 2026-03-10T20:00, 2026-03-11T08:01, false,  50.0",
                "CT29, 2026-03-10T10:00, 2026-03-11T09:00, false,  50.0",
                "CT30, 2026-03-10T10:00, 2026-03-12T09:00, false, 100.0",
                "CT31, 2026-03-10T10:00, 2026-03-13T09:00, false, 150.0",
                "CT32, 2026-03-10T20:00, 2026-03-11T08:01, true,   25.0",
                "CT33, 2026-03-10T10:00, 2026-03-12T09:00, true,   50.0",
                "CT34, 2026-12-31T20:00, 2027-01-01T09:00, false,  50.0",
                "CT35, 2028-02-28T20:00, 2028-02-29T09:00, false,  50.0"
        })
        void deveCobrarPorPernoite(String caso, LocalDateTime entrada, LocalDateTime saida,
                                   boolean vip, double esperado) {
            assertEquals(esperado, calculadora.calcular(new Ticket(entrada, saida, vip)), DELTA, caso);
        }
    }

    @Nested
    @DisplayName("Janelas de operacao e datas invalidas")
    class EntradasInvalidas {

        @ParameterizedTest(name = "{0}: {1} ate {2} => EstacionamentoException")
        @CsvSource({
                "CT01, 2026-03-10T07:59, 2026-03-10T10:00",
                "CT04, 2026-03-10T00:00, 2026-03-10T10:00",
                "CT06, 2026-03-10T22:00, 2026-03-11T02:00",
                "CT07, 2026-03-10T22:00, 2026-03-11T07:59",
                "CT09, 2026-03-10T12:00, 2026-03-10T11:00"
        })
        void deveRejeitarHorarioForaDaJanela(String caso, LocalDateTime entrada, LocalDateTime saida) {
            assertThrows(EstacionamentoException.class,
                    () -> calculadora.calcular(new Ticket(entrada, saida, false)), caso);
        }

        @ParameterizedTest(name = "{0}: {1}/{2}/{3} => EstacionamentoException")
        @CsvSource({
                "CT10, 30,  2, 2026",
                "CT11, 10, 13, 2026"
        })
        void deveRejeitarDataInexistente(String caso, int dia, int mes, int ano) {
            assertThrows(EstacionamentoException.class,
                    () -> Ticket.de(dia, mes, ano, 10, 0, dia, mes, ano, 11, 0, false), caso);
        }
    }
}
