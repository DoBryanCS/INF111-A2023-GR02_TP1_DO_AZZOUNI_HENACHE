package com.echecs.pieces;

import com.echecs.Position;
import com.echecs.util.EchecsUtil;

public class Pion extends Piece {
    public Pion(char couleur) {
        super(couleur);
    }

    @Override
    public boolean peutSeDeplacer(Position pos1, Position pos2, Piece[][] echiquier) {
        byte ligne1 = EchecsUtil.indiceLigne(pos1);
        byte ligne2 = EchecsUtil.indiceLigne(pos2);
        byte colonne1 = EchecsUtil.indiceColonne(pos1);
        byte colonne2 = EchecsUtil.indiceColonne(pos2);

        if (this.couleur == 'b') {
            return peutSeDeplacerBlanc(ligne1, ligne2, colonne1, colonne2, echiquier);
        } else if (this.couleur == 'n') {
            return peutSeDeplacerNoir(ligne1, ligne2, colonne1, colonne2, echiquier);
        }

        return false;
    }

    private boolean peutSeDeplacerBlanc(byte ligne1, byte ligne2, byte colonne1, byte colonne2, Piece[][] echiquier) {
        // 2 cases depuis la ligne de départ
        if (ligne1 == 6 && ligne2 == 4 && colonne2 == colonne1) {
            return echiquier[5][colonne2] == null && echiquier[4][colonne2] == null;
        }
        // 1 case
        else if (ligne2 == ligne1 - 1 && colonne2 == colonne1) {
            return echiquier[ligne2][colonne2] == null;
        }
        // diagonale
        else if (ligne2 == ligne1 - 1 && Math.abs(colonne2 - colonne1) == 1) {
            return echiquier[ligne2][colonne2] != null;
        }

        return false;
    }

    private boolean peutSeDeplacerNoir(byte ligne1, byte ligne2, byte colonne1, byte colonne2, Piece[][] echiquier) {
        // 2 cases depuis la ligne de départ
        if (ligne1 == 1 && ligne2 == 3 && colonne2 == colonne1) {
            return echiquier[2][colonne2] == null && echiquier[3][colonne2] == null;
        }
        // 1 case
        else if (ligne2 == ligne1 + 1 && colonne2 == colonne1) {
            return echiquier[ligne2][colonne2] == null;
        }
        // diagonale
        else if (ligne2 == ligne1 + 1 && Math.abs(colonne2 - colonne1) == 1) {
            return echiquier[ligne2][colonne2] != null;
        }

        return false;
    }
}
