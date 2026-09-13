package br.pucrs.verival.estacionamento;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class CalculadoraTarifa {

    public static final int MINUTOS_CORTESIA = 20;
    public static final double VALOR_PRIMEIRA_HORA = 15.0;
    public static final double VALOR_HORA_ADICIONAL = 5.0;
    public static final double VALOR_PERNOITE = 50.0;
    public static final double DESCONTO_VIP = 0.5;

    public double calcular(Ticket ticket) {
        validar(ticket);

        double valor;
        if (ehPernoite(ticket)) {
            valor = contarPernoites(ticket) * VALOR_PERNOITE;
        } else {
            valor = calcularPorHora(minutos(ticket));
        }

        if (ticket.isVip()) {
            valor = valor * DESCONTO_VIP;
        }
        return valor;
    }

    private void validar(Ticket ticket) {
        if (ticket.getSaida().isBefore(ticket.getEntrada())) {
            throw new EstacionamentoException("Saida anterior a entrada");
        }
        int horaEntrada = ticket.getEntrada().getHour();
        if (horaEntrada < 8) {
            throw new EstacionamentoException("Entrada nao permitida neste horario");
        }
    }

    private long minutos(Ticket ticket) {
        return ChronoUnit.MINUTES.between(ticket.getEntrada(), ticket.getSaida());
    }

    private double calcularPorHora(long minutos) {
        if (minutos < MINUTOS_CORTESIA) {
            return 0.0;
        }
        if (minutos < 60) {
            return VALOR_PRIMEIRA_HORA;
        }
        long horasAdicionais = (minutos - 60) / 60 + 1;
        return VALOR_PRIMEIRA_HORA + horasAdicionais * VALOR_HORA_ADICIONAL;
    }

    private boolean ehPernoite(Ticket ticket) {
        return ticket.getSaida().toLocalDate().isAfter(ticket.getEntrada().toLocalDate());
    }

    private long contarPernoites(Ticket ticket) {
        LocalDateTime entrada = ticket.getEntrada();
        LocalDateTime saida = ticket.getSaida();
        return ChronoUnit.DAYS.between(entrada.toLocalDate(), saida.toLocalDate());
    }
}
