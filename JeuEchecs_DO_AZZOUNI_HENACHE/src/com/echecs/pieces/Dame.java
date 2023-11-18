package com.echecs.pieces;

import com.echecs.Position;
import com.echecs.util.EchecsUtil;

public class Dame extends com.echecs.pieces.Piece {
    public Dame(char couleur) {
        super(couleur);
    }
    @Override
    public boolean peutSeDeplacer(Position pos1, Position pos2, com.echecs.pieces.Piece[][] echiquier) {
        byte ligne1 = EchecsUtil.indiceLigne(pos1);
        byte ligne2 = EchecsUtil.indiceLigne(pos2);
        byte colonne1 = EchecsUtil.indiceColonne(pos1);
        byte colonne2 = EchecsUtil.indiceColonne(pos2);

        // Vérification du déplacement vertical
        if (colonne1 == colonne2 && peutSeDeplacerVerticalement(ligne1, ligne2, colonne2, echiquier)) {
            return true;
        }

        // Vérification du déplacement horizontal
        if (ligne1 == ligne2 && peutSeDeplacerHorizontalement(colonne1, colonne2, ligne2, echiquier)) {
            return true;
        }

        // Vérification du déplacement en diagonale
        return pos1.estSurLaMemeDiagonaleQue(pos2) && peutSeDeplacerEnDiagonale(ligne1, colonne1, ligne2, colonne2, echiquier);
    }

    private boolean peutSeDeplacerVerticalement(byte ligne1, byte ligne2, byte colonne2, com.echecs.pieces.Piece[][] echiquier) {
        byte start = (byte) (Math.min(ligne1, ligne2) + 1);
        byte end = (byte) Math.max(ligne1, ligne2);

        for (byte i = start; i < end; i++) {
            if (echiquier[i][colonne2] != null) {
                return false; // Il y a une pièce sur le chemin
            }
        }

        return true;
    }

    private boolean peutSeDeplacerHorizontalement(byte colonne1, byte colonne2, byte ligne2, com.echecs.pieces.Piece[][] echiquier) {
        byte start = (byte) (Math.min(colonne1, colonne2) + 1);
        byte end = (byte) Math.max(colonne1, colonne2);

        for (byte j = start; j < end; j++) {
            if (echiquier[ligne2][j] != null) {
                return false; // Il y a une pièce sur le chemin
            }
        }

        return true;
    }

    private boolean peutSeDeplacerEnDiagonale(byte ligne1, byte colonne1, byte ligne2, byte colonne2, Piece[][] echiquier) {
        // Déplacement en diagonale
        byte stepLigne = (byte) ((ligne2 > ligne1) ? 1 : -1);
        byte stepColonne = (byte) ((colonne2 > colonne1) ? 1 : -1);

        for (byte i = (byte) (ligne1 + stepLigne), j = (byte) (colonne1 + stepColonne); i != ligne2 || j != colonne2; i += stepLigne, j += stepColonne) {
            if (echiquier[i][j] != null) {
                return false; // Il y a une pièce sur le chemin
            }
        }

        return true;
    }
}
