package com.sherolero.tales.reploks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Questions {
    private final String pergunta;
    private final ArrayList<String> respostas;

    private Questions(final String pergunta, final ArrayList<String> respostas) {
        this.pergunta = pergunta;
        this.respostas = respostas;
    }

    public static Questions parse(final String input) throws IllegalArgumentException {
        final String[] questionComponents;
        final String pergunta;
        final ArrayList<String> respostas = new ArrayList<String>();

        if (input != null) {
            questionComponents = input.split(";");
        } else {
            throw new IllegalArgumentException("Pergunta enviada possui valor null");
        }

        if (questionComponents.length > 3) {
            pergunta = questionComponents[0].trim();

            for (int i = 1; i < questionComponents.length; i++) {
                respostas.add(questionComponents[i].trim());
            }
        } else {
            throw new IllegalArgumentException("Questao com erro de formatacao. Poucas partes");
        }
        return new Questions(pergunta, respostas);
    }

    public final String getPergunta() {
        return pergunta;
    }

    public List<String> getRespostas() {
        final ArrayList<String> respostas = new ArrayList<String>(this.respostas);
        Collections.shuffle(respostas);
        return respostas;
    }

    public final String getAnswer() {
        return respostas.get(0);
    }
}
