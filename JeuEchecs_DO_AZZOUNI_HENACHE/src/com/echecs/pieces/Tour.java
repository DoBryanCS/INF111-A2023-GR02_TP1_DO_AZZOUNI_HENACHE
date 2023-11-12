package com.echecs.pieces;

import com.echecs.Position;
import com.echecs.util.EchecsUtil;

public class Tour extends Piece {
    private boolean aBouge;
    public Tour(char couleur, boolean aBouge) {
        super(couleur);
        this.aBouge = aBouge;
    }

    public boolean getABouge() {
        return aBouge;
    }

    public void setABouge(boolean aBouge) {
        this.aBouge = aBouge;
    }

    @Override
    public boolean peutSeDeplacer(Position pos1, Position pos2, Piece[][] echiquier) {
        byte ligne1 = EchecsUtil.indiceLigne(pos1);
        byte ligne2 = EchecsUtil.indiceLigne(pos2);
        byte colonne1 = EchecsUtil.indiceColonne(pos1);
        byte colonne2 = EchecsUtil.indiceColonne(pos2);

        // Vérification du déplacement vertical
        if (colonne1 == colonne2 && peutSeDeplacerVerticalement(ligne1, ligne2, colonne2, echiquier)) {
            aBouge = true;
            return true;
        }

        // Vérification du déplacement horizontal
        if (ligne1 == ligne2 && peutSeDeplacerHorizontalement(colonne1, colonne2, ligne2, echiquier)) {
            aBouge = true;
            return true;
        }

        return false;
    }

    private boolean peutSeDeplacerVerticalement(byte ligne1, byte ligne2, byte colonne2, Piece[][] echiquier) {
        byte start = (byte) (Math.min(ligne1, ligne2) + 1);
        byte end = (byte) Math.max(ligne1, ligne2);

        for (byte i = start; i < end; i++) {
            if (echiquier[i][colonne2] != null) {
                return false; // Il y a une pièce sur le chemin
            }
        }

        return true;
    }

    private boolean peutSeDeplacerHorizontalement(byte colonne1, byte colonne2, byte ligne2, Piece[][] echiquier) {
        byte start = (byte) (Math.min(colonne1, colonne2) + 1);
        byte end = (byte) Math.max(colonne1, colonne2);

        for (byte j = start; j < end; j++) {
            if (echiquier[ligne2][j] != null) {
                return false; // Il y a une pièce sur le chemin
            }
        }

        return true;
    }
}
