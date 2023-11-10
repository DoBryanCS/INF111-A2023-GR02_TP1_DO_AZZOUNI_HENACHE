package com.echecs.pieces;

import com.echecs.Position;
import com.echecs.util.EchecsUtil;

public class Fou extends Piece{
    public Fou(char couleur) {
        super(couleur);
    }
    @Override
    public boolean peutSeDeplacer(Position pos1, Position pos2, Piece[][] echiquier) {
        byte ligne1 = EchecsUtil.indiceLigne(pos1);
        byte ligne2 = EchecsUtil.indiceLigne(pos2);
        byte colonne1 = EchecsUtil.indiceColonne(pos1);
        byte colonne2 = EchecsUtil.indiceColonne(pos2);

        // Vérification du déplacement en diagonale
        return pos1.estSurLaMemeDiagonaleQue(pos2) && peutSeDeplacerEnDiagonale(ligne1, colonne1, ligne2, colonne2, echiquier);
    }

    private boolean peutSeDeplacerEnDiagonale(byte ligne1, byte colonne1, byte ligne2, byte colonne2, Piece[][] echiquier) {
        byte startLigne = (byte) (Math.min(ligne1, ligne2) + 1);
        byte endLigne = (byte) Math.max(ligne1, ligne2);
        byte startColonne = (byte) (Math.min(colonne1, colonne2) + 1);
        byte endColonne = (byte) Math.max(colonne1, colonne2);

        for (byte i = startLigne, j = startColonne; i < endLigne && j < endColonne; i++, j++) {
            if (echiquier[i][j] != null) {
                return false; // Il y a une pièce sur le chemin
            }
        }

        return true;
    }
}
