package br.pucrs.verival.estacionamento;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class CalculadoraTarifa {

    public static final int MINUTOS_CORTESIA = 20;
    public static final int MINUTOS_PRIMEIRA_HORA = 60;
    public static final int HORA_ABERTURA = 8;
    public static final int HORA_INICIO_BLOQUEIO_SAIDA = 2;
    public static final int HORA_FIM_BLOQUEIO_SAIDA = 7;
    public static final double VALOR_PRIMEIRA_HORA = 15.0;
    public static final double VALOR_HORA_ADICIONAL = 5.0;
    public static final double VALOR_PERNOITE = 50.0;
    public static final double FATOR_VIP = 0.5;

    public double calcular(Ticket ticket) {
        validar(ticket);

        double valor = ehPernoite(ticket)
                ? contarPernoites(ticket) * VALOR_PERNOITE
                : calcularPorHora(minutosDePermanencia(ticket));

        if (ticket.isVip()) {
            valor = valor * FATOR_VIP;
        }
        return valor;
    }

    private void validar(Ticket ticket) {
        if (ticket.getSaida().isBefore(ticket.getEntrada())) {
            throw new EstacionamentoException("Saida anterior a entrada");
        }
        int horaEntrada = ticket.getEntrada().getHour();
        if (horaEntrada < HORA_ABERTURA) {
            throw new EstacionamentoException(
                    "Entrada permitida somente das 08:00 as 23:59, recebido " + ticket.getEntrada());
        }
        int horaSaida = ticket.getSaida().getHour();
        if (horaSaida >= HORA_INICIO_BLOQUEIO_SAIDA && horaSaida <= HORA_FIM_BLOQUEIO_SAIDA) {
            throw new EstacionamentoException(
                    "Saida nao permitida das 02:00 as 07:59, recebido " + ticket.getSaida());
        }
    }

    private long minutosDePermanencia(Ticket ticket) {
        return ChronoUnit.MINUTES.between(ticket.getEntrada(), ticket.getSaida());
    }

    private double calcularPorHora(long minutos) {
        if (minutos <= MINUTOS_CORTESIA) {
            return 0.0;
        }
        if (minutos <= MINUTOS_PRIMEIRA_HORA) {
            return VALOR_PRIMEIRA_HORA;
        }
        long excedente = minutos - MINUTOS_PRIMEIRA_HORA;
        long intervalosAdicionais = (excedente + MINUTOS_PRIMEIRA_HORA - 1) / MINUTOS_PRIMEIRA_HORA;
        return VALOR_PRIMEIRA_HORA + intervalosAdicionais * VALOR_HORA_ADICIONAL;
    }

    private boolean ehPernoite(Ticket ticket) {
        LocalDateTime limite = ticket.getEntrada()
                .toLocalDate()
                .plusDays(1)
                .atTime(HORA_ABERTURA, 0);
        return ticket.getSaida().isAfter(limite);
    }

    private long contarPernoites(Ticket ticket) {
        return ChronoUnit.DAYS.between(
                ticket.getEntrada().toLocalDate(),
                ticket.getSaida().toLocalDate());
    }
}
