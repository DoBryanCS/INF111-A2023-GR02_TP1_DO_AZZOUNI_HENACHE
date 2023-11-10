package com.chat.client;

public class EtatPartieEchecs {
    private char[][] etatEchiquier;

    public EtatPartieEchecs() {
        etatEchiquier = new char[][]{
                {'t', 'c', 'f', 'd', 'r', 'f', 'c', 't'},
                {'p', 'p', 'p', 'p', 'p', 'p', 'p', 'p'},
                {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
                {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
                {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
                {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
                {'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P'},
                {'T', 'C', 'F', 'D', 'R', 'F', 'C', 'T'}
        };
    }
    public char[][] getEtatEchiquier() {
        return etatEchiquier;
    }

    public void setEtatEchiquier(char[][] etatEchiquier) {
        this.etatEchiquier = etatEchiquier;
    }

    @Override
    public String toString() {
        String result = "";
        for (int i = 0; i < 8; i++) {
            result += (byte)(8 - i) + " ";
            for (int j = 0; j < 8; j++) {
                result += (etatEchiquier[i][j] == ' ' ? ". " : etatEchiquier[i][j] + " ");
            }
            result += "\n";
        }
        result += "  ";
        for (byte j = 0; j < 8; j++) {
            result += (char)('a' + j) + " ";
        }
        return result;
    }

}
