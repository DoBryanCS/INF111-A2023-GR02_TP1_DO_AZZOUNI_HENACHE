package com.echecs.pieces;

import com.echecs.Position;

public class Roi extends Piece {
    private boolean aBouge;
    public Roi(char couleur, boolean aBouge) {
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

        if (pos1.estVoisineDe(pos2)) {
            aBouge = true;
            return true; // Déplacement simple
        }

        return false;
    }
}
