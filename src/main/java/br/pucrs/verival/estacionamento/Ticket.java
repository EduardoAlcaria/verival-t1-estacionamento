package br.pucrs.verival.estacionamento;

import java.time.DateTimeException;
import java.time.LocalDateTime;

public class Ticket {

    private final LocalDateTime entrada;
    private final LocalDateTime saida;
    private final boolean vip;

    public Ticket(LocalDateTime entrada, LocalDateTime saida, boolean vip) {
        if (entrada == null || saida == null) {
            throw new EstacionamentoException("Data de entrada e de saida sao obrigatorias");
        }
        this.entrada = entrada;
        this.saida = saida;
        this.vip = vip;
    }

    public static Ticket de(int diaEntrada, int mesEntrada, int anoEntrada,
                            int horaEntrada, int minutoEntrada,
                            int diaSaida, int mesSaida, int anoSaida,
                            int horaSaida, int minutoSaida,
                            boolean vip) {
        return new Ticket(
                construir(diaEntrada, mesEntrada, anoEntrada, horaEntrada, minutoEntrada, "entrada"),
                construir(diaSaida, mesSaida, anoSaida, horaSaida, minutoSaida, "saida"),
                vip);
    }

    private static LocalDateTime construir(int dia, int mes, int ano, int hora, int minuto, String rotulo) {
        try {
            return LocalDateTime.of(ano, mes, dia, hora, minuto);
        } catch (DateTimeException e) {
            throw new EstacionamentoException("Data/hora de " + rotulo + " invalida: "
                    + dia + "/" + mes + "/" + ano + " " + hora + ":" + minuto);
        }
    }

    public LocalDateTime getEntrada() {
        return entrada;
    }

    public LocalDateTime getSaida() {
        return saida;
    }

    public boolean isVip() {
        return vip;
    }
}
