package br.pucrs.verival.estacionamento;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class CalculadoraTarifa {

    private static final LocalTime ABERTURA = LocalTime.of(8, 0);
    private static final LocalTime INICIO_SAIDA_PROIBIDA = LocalTime.of(2, 0);
    private static final LocalTime FIM_SAIDA_PROIBIDA = LocalTime.of(7, 59);

    private static final int MINUTOS_CORTESIA = 20;
    private static final int MINUTOS_TARIFA_FIXA = 60;
    private static final double VALOR_TARIFA_FIXA = 15.0;
    private static final double VALOR_HORA_ADICIONAL = 5.0;
    private static final double VALOR_PERNOITE = 50.0;
    private static final double DESCONTO_VIP = 0.5;

    public double calcular(LocalDateTime entrada, LocalDateTime saida, boolean vip) {
        validarEntrada(entrada);
        validarSaida(saida);

        if (!saida.isAfter(entrada)) {
            throw new IllegalArgumentException("Data/hora de saída deve ser posterior à de entrada");
        }

        long pernoites = contarPernoites(entrada, saida);

        double valor = calcularPerNoite(pernoites, entrada, saida);

        return calcularDescontoVip(valor, vip);
    }

    private double calcularPerNoite(long pernoites, LocalDateTime entrada, LocalDateTime saida){
        if (pernoites > 0){
            return pernoites * VALOR_PERNOITE;
        }

        return calcularPorPermanencia(Duration.between(entrada, saida).toMinutes());
    }

    private double calcularDescontoVip(double valor, boolean vip){
        if (vip){
            return valor * DESCONTO_VIP;
        }
        return valor;
    }
    private double calcularPorPermanencia(long minutos) {
        if (minutos <= MINUTOS_CORTESIA) {
            return 0.0;
        }
        if (minutos <= MINUTOS_TARIFA_FIXA) {
            return VALOR_TARIFA_FIXA;
        }
        long minutosExcedentes = minutos - MINUTOS_TARIFA_FIXA;
        long horasAdicionais = (minutosExcedentes + MINUTOS_TARIFA_FIXA - 1) / MINUTOS_TARIFA_FIXA;
        return VALOR_TARIFA_FIXA + horasAdicionais * VALOR_HORA_ADICIONAL;
    }

    private long contarPernoites(LocalDateTime entrada, LocalDateTime saida) {
        LocalDateTime limite = entrada.toLocalDate().plusDays(1).atTime(ABERTURA);
        long pernoites = 0;

        while (!saida.isBefore(limite)) {
            pernoites++;
            limite = limite.plusDays(1);
        }
        return pernoites;
    }

    private void validarEntrada(LocalDateTime entrada) {
        if (entrada.toLocalTime().isBefore(ABERTURA)) {
            throw new IllegalArgumentException("Entrada não permitida antes das 08:00");
        }
    }

    private void validarSaida(LocalDateTime saida) {
        LocalTime hora = saida.toLocalTime();
        boolean dentroJanelaProibida = !hora.isBefore(INICIO_SAIDA_PROIBIDA) && !hora.isAfter(FIM_SAIDA_PROIBIDA);
        if (dentroJanelaProibida) {
            throw new IllegalArgumentException("Saída não permitida entre 02:00 e 07:59");
        }
    }
}
