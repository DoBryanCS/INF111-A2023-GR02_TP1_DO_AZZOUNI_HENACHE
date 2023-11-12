package com.echecs.pieces;

import com.echecs.Position;
import com.echecs.util.EchecsUtil;

public class Cavalier extends Piece {
    public Cavalier(char couleur) {
        super(couleur);
    }
    @Override
    public boolean peutSeDeplacer(Position pos1, Position pos2, Piece[][] echiquier) {
        byte ligne1 = EchecsUtil.indiceLigne(pos1);
        byte ligne2 = EchecsUtil.indiceLigne(pos2);
        byte colonne1 = EchecsUtil.indiceColonne(pos1);
        byte colonne2 = EchecsUtil.indiceColonne(pos2);

        int diffLigne = Math.abs(ligne2 - ligne1);
        int diffColonne = Math.abs(colonne2 - colonne1);

        // Vérification du mouvement en "L"
        return (diffLigne == 2 && diffColonne == 1) || (diffLigne == 1 && diffColonne == 2);
    }
}
