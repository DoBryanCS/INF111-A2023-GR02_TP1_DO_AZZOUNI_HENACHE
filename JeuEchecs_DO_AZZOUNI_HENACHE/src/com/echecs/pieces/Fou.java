package com.echecs.pieces;

import com.echecs.Position;
import com.echecs.util.EchecsUtil;

public class Fou extends com.echecs.pieces.Piece {
    public Fou(char couleur) {
        super(couleur);
    }
    @Override
    public boolean peutSeDeplacer(Position pos1, Position pos2, com.echecs.pieces.Piece[][] echiquier) {
        byte ligne1 = EchecsUtil.indiceLigne(pos1);
        byte ligne2 = EchecsUtil.indiceLigne(pos2);
        byte colonne1 = EchecsUtil.indiceColonne(pos1);
        byte colonne2 = EchecsUtil.indiceColonne(pos2);

        // Vérification du déplacement en diagonale
        return pos1.estSurLaMemeDiagonaleQue(pos2) && peutSeDeplacerEnDiagonale(ligne1, colonne1, ligne2, colonne2, echiquier);
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
