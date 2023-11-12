package com.echecs;

import com.echecs.pieces.*;
import com.echecs.util.EchecsUtil;

/**
 * Représente une partie de jeu d'échecs. Orcheste le déroulement d'une partie :
 * déplacement des pièces, vérification d'échec, d'échec et mat,...
 *
 * @author Abdelmoumène Toudeft (Abdelmoumene.Toudeft@etsmtl.ca)
 * @version 1.0
 * @since 2023-09-01
 */
public class PartieEchecs {
    /**
     * Grille du jeu d'échecs. La ligne 0 de la grille correspond à la ligne
     * 8 de l'échiquier. La colonne 0 de la grille correspond à la colonne a
     * de l'échiquier.
     */
    private Piece[][] echiquier;
    private String aliasJoueur1, aliasJoueur2;
    private char couleurJoueur1, couleurJoueur2;

    private boolean roqueRoiVientEtreFait = false;

    private boolean roqueDameVientEtreFait = false;

    /**
     * La couleur de celui à qui c'est le tour de jouer (n ou b).
     */
    private char tour = 'b'; //Les blancs commencent toujours
    /**
     * Crée un échiquier de jeu d'échecs avec les pièces dans leurs positions
     * initiales de début de partie.
     * Répartit au hasard les couleurs n et b entre les 2 joueurs.
     */
    public PartieEchecs() {
        echiquier = new Piece[8][8];
        //Placement des pièces :

        // Pions
        for (byte colonne = 0; colonne <= 7; colonne++) {
            echiquier[1][colonne] = new Pion('n');
            echiquier[6][colonne] = new Pion('b');
        }

        // Tours
        echiquier[0][0] = new Tour('n', false);
        echiquier[0][7] = new Tour('n', false);

        echiquier[7][0] = new Tour('b', false);
        echiquier[7][7] = new Tour('b', false);

        // Cavaliers
        echiquier[0][1] = new Cavalier('n');
        echiquier[0][6] = new Cavalier('n');
        echiquier[7][1] = new Cavalier('b');
        echiquier[7][6] = new Cavalier('b');

        // Fous
        echiquier[0][2] = new Fou('n');
        echiquier[0][5] = new Fou('n');
        echiquier[7][2] = new Fou('b');
        echiquier[7][5] = new Fou('b');

        // Dames
        echiquier[0][3] = new Dame('n');
        echiquier[7][3] = new Dame('b');

        // Rois
        echiquier[0][4] = new Roi('n', false);
        echiquier[7][4] = new Roi('b', false);

        tour = 'b'; // Les blancs commencent toujours
    }

    /**
     * Change la main du jeu (de n à b ou de b à n).
     */
    public void changerTour() {
        if (tour=='b')
            tour = 'n';
        else
            tour = 'b';
    }
    /**
     * Tente de déplacer une pièce d'une position à une autre sur l'échiquier.
     * Le déplacement peut échouer pour plusieurs raisons, selon les règles du
     * jeu d'échecs. Par exemples :
     *  Une des positions n'existe pas;
     *  Il n'y a pas de pièce à la position initiale;
     *  La pièce de la position initiale ne peut pas faire le mouvement;
     *  Le déplacement met en échec le roi de la même couleur que la pièce.
     *
     * @param initiale Position la position initiale
     * @param finale Position la position finale
     *
     * @return boolean true, si le déplacement a été effectué avec succès, false sinon
     */
    public boolean deplace(com.echecs.Position initiale, com.echecs.Position finale) {
        byte ligne1 = EchecsUtil.indiceLigne(initiale);
        byte ligne2 = EchecsUtil.indiceLigne(finale);
        byte colonne1 = EchecsUtil.indiceColonne(initiale);
        byte colonne2 = EchecsUtil.indiceColonne(finale);

        // Vérifier la validité des positions
        if (!EchecsUtil.positionValide(initiale) || !EchecsUtil.positionValide(finale))
            return false;

        // Vérifier si une pièce existe à la position initiale
        if (echiquier[ligne1][colonne1] == null)
            return false;

        // Récupérer les pièces aux positions initiale et finale
        Piece pieceADeplacer = echiquier[ligne1][colonne1];
        Piece pieceFinale = echiquier[ligne2][colonne2];

        // Vérifier si la couleur de la pièce à déplacer correspond à la couleur du tour
        if (pieceADeplacer.getCouleur() != tour)
            return false;

        // Vérifier si la couleur de la pièce finale est la même que la couleur du tour
        if (pieceFinale != null && pieceFinale.getCouleur() == tour)
            return false;

        // Vérifier le Roque
        if (pieceADeplacer instanceof Roi) {
            Roi roi = (Roi) pieceADeplacer;

            // Roque du côté roi
            if (colonne2 - colonne1 == 2 && ligne1 == ligne2) {
                return faireRoqueCoteRoi(roi, pieceFinale, ligne1, colonne1, ligne2, colonne2);
            }

            // Roque du côté dame
            if (colonne2 - colonne1 == -2 && ligne1 == ligne2) {
                return faireRoqueCoteDame(roi, pieceFinale, ligne1, colonne1, ligne2, colonne2);
            }
        }

        // Déplacement normal
        if (pieceADeplacer.peutSeDeplacer(initiale, finale, echiquier)) {

            // Manger la pièce adverse
            if (pieceFinale != null) {

                echiquier[ligne2][colonne2] = pieceADeplacer;
                echiquier[ligne1][colonne1] = null;

                char estEnEchec = estEnEchec();
                // Vérifier si le roi est en échec après le déplacement
                if (estEnEchec == tour) {
                    echiquier[ligne2][colonne2] = pieceFinale; // Annuler le déplacement
                    echiquier[ligne1][colonne1] = pieceADeplacer;
                    return false;
                }
            } else {
                // Déplacement simple
                echiquier[ligne2][colonne2] = pieceADeplacer;
                echiquier[ligne1][colonne1] = null;

                char estEnEchec = estEnEchec();
                // Vérifier si la piece est en échec après le déplacement
                if (estEnEchec == tour) {
                    echiquier[ligne2][colonne2] = pieceFinale; // Annuler le déplacement
                    echiquier[ligne1][colonne1] = pieceADeplacer;
                    return false;
                }
            }

            // Vérifier la promotion automatique en dame
            if (pieceADeplacer instanceof Pion) {
                if ((pieceADeplacer.getCouleur() == 'b' && ligne2 == 0) || (pieceADeplacer.getCouleur() == 'n' && ligne2 == 7)) {
                    echiquier[ligne2][colonne2] = new Dame(tour); // Remplacez par votre classe Dame
                }
            }

            // Changer le tour
            changerTour();

            return true;
        }

        return false;
    }

    // Roque du côté roi
    private boolean faireRoqueCoteRoi(Roi roi, Piece pieceFinale, int ligne1, int colonne1, int ligne2, int colonne2) {

        // Vérifier les conditions du roque
        if (roi.getABouge() || echiquier[ligne1][7] == null || !(echiquier[ligne1][7] instanceof Tour) || ((Tour) echiquier[ligne1][7]).getABouge()) {
            return false;
        }

        // Vérifier s'il y a des pièces entre le roi et la tour
        for (int i = colonne1 + 1; i < colonne2 + 1; i++) {
            if (echiquier[ligne1][i] != null) {
                return false;
            }
        }

        // Déplacer le roi
        echiquier[ligne2][colonne2] = roi;
        echiquier[ligne1][colonne1] = null;
        roi.setABouge(true);

        // Déplacer la tour
        echiquier[ligne2][colonne2 - 1] = echiquier[ligne1][7];
        echiquier[ligne1][7] = null;
        ((Tour) echiquier[ligne2][colonne2 - 1]).setABouge(true);

        char estEnEchec = estEnEchec();
        // Vérifier si le roi est en échec
        if (estEnEchec == roi.getCouleur()) {
            echiquier[ligne2][colonne2] = pieceFinale; // Annuler le déplacement du roi
            echiquier[ligne1][colonne1] = roi;
            roi.setABouge(false);

            echiquier[ligne1][7] = echiquier[ligne2][colonne2 - 1]; // Annuler le déplacement de la tour
            echiquier[ligne2][colonne2 - 1] = null;
            ((Tour) echiquier[ligne2][colonne2 - 1]).setABouge(false);

            return false;
        }

        roqueRoiVientEtreFait = true;

        // Changer le tour
        changerTour();

        return true;
    }

    // Roque du côté dame
    private boolean faireRoqueCoteDame(Roi roi, Piece pieceFinale, int ligne1, int colonne1, int ligne2, int colonne2) {
        // Vérifier les conditions du roque
        if (roi.getABouge() || echiquier[ligne1][0] == null || !(echiquier[ligne1][0] instanceof Tour) || ((Tour) echiquier[ligne1][0]).getABouge()) {
            return false;
        }

        // Vérifier s'il y a des pièces entre le roi et la tour
        for (int i = colonne2 + 1; i < colonne1 - 1; i++) {
            if (echiquier[ligne1][i] != null) {
                return false;
            }
        }

        // Déplacer le roi
        echiquier[ligne2][colonne2] = roi;
        echiquier[ligne1][colonne1] = null;
        roi.setABouge(true);

        // Déplacer la tour
        echiquier[ligne2][colonne2 + 1] = echiquier[ligne1][0];
        echiquier[ligne1][0] = null;
        ((Tour) echiquier[ligne2][colonne2 + 1]).setABouge(true);

        char estEnEchec = estEnEchec();
        // Vérifier si le roi est en échec
        if (estEnEchec == roi.getCouleur()) {
            echiquier[ligne2][colonne2] = pieceFinale; // Annuler le déplacement du roi
            echiquier[ligne1][colonne1] = roi;
            roi.setABouge(false);

            echiquier[ligne1][0] = echiquier[ligne2][colonne2 + 1]; // Annuler le déplacement de la tour
            echiquier[ligne2][colonne2 + 1] = null;
            ((Tour) echiquier[ligne2][colonne2 + 1]).setABouge(false);

            return false;
        }

        roqueDameVientEtreFait = true;

        // Changer le tour
        changerTour();

        return true;
    }

    /**
     * Vérifie si un roi est en échec et, si oui, retourne sa couleur sous forme
     * d'un caractère n ou b.
     * Si la couleur du roi en échec est la même que celle de la dernière pièce
     * déplacée, le dernier déplacement doit être annulé.
     * Les 2 rois peuvent être en échec en même temps. Dans ce cas, la méthode doit
     * retourner la couleur de la pièce qui a été déplacée en dernier car ce
     * déplacement doit être annulé.
     *
     * @return char Le caractère n, si le roi noir est en échec, le caractère b,
     * si le roi blanc est en échec, tout autre caractère, sinon.
     */
    public char estEnEchec() {
        // Recherche des positions des rois
        byte roiNoirLigne = -1;
        byte roiNoirColonne = -1;
        byte roiBlancLigne = -1;
        byte roiBlancColonne = -1;

        boolean roiBlancEnEchec = false;
        boolean roiNoirEnEchec = false;

        for (byte i = 0; i < 8; i++) {
            for (byte j = 0; j < 8; j++) {
                if (echiquier[i][j] instanceof Roi) {
                    if (echiquier[i][j].getCouleur() == 'n') {
                        roiNoirLigne = i;
                        roiNoirColonne = j;
                    } else if (echiquier[i][j].getCouleur() == 'b') {
                        roiBlancLigne = i;
                        roiBlancColonne = j;
                    }
                }
            }
        }

        // Vérification des attaques sur le roi noir
        if (roiNoirLigne != -1 && roiNoirColonne != -1) {
            for (byte i = 0; i < 8; i++) {
                for (byte j = 0; j < 8; j++) {
                    if (echiquier[i][j] != null && echiquier[i][j].getCouleur() != 'n') {
                        if (echiquier[i][j].peutSeDeplacer(EchecsUtil.getPosition(i,j), EchecsUtil.getPosition(roiNoirLigne,roiNoirColonne), echiquier)) {
                            roiNoirEnEchec = true; // Le roi noir est en échec
                        }
                    }
                }
            }
        }

        // Vérification des attaques sur le roi blanc
        if (roiBlancLigne != -1 && roiBlancColonne != -1) {
            for (byte i = 0; i < 8; i++) {
                for (byte j = 0; j < 8; j++) {
                    if (echiquier[i][j] != null && echiquier[i][j].getCouleur() != 'b') {
                        if (echiquier[i][j].peutSeDeplacer(EchecsUtil.getPosition(i,j), EchecsUtil.getPosition(roiBlancLigne,roiBlancColonne), echiquier)) {
                            roiBlancEnEchec = true; // Le roi blanc est en échec
                        }
                    }
                }
            }
        }

        if (roiNoirEnEchec && roiBlancEnEchec) {
            return tour; // Retourner la couleur de la dernière pièce déplacée
        }

        // Vérification des attaques sur le roi blanc
        if (roiBlancEnEchec) {
            return 'b';
        }

        // Vérification des attaques sur le roi noir
        if (roiNoirEnEchec) {
            return 'n';
        }

        return 'a'; // Aucun roi en échec
    }

    /**
     * Vérifie si le joueur de la couleur spécifiée est en échec et mat.
     *
     * @param couleur La couleur du joueur à vérifier ('n' pour noir, 'b' pour blanc).
     * @return true si le joueur est en échec et mat, false sinon.
     */
    public boolean estEnEchecEtMat(char couleur) {
        if (estEnEchec() != couleur) {
            return false; // Le joueur n'est pas en échec, donc pas en échec et mat.
        }

        // Parcourir toutes les pièces du joueur en cours
        for (byte i = 0; i < 8; i++) {
            for (byte j = 0; j < 8; j++) {
                if (echiquier[i][j] != null && echiquier[i][j].getCouleur() == couleur) {
                    // Essayer tous les mouvements possibles pour cette pièce
                    for (byte x = 0; x < 8; x++) {
                        for (byte y = 0; y < 8; y++) {
                            com.echecs.Position initiale = EchecsUtil.getPosition(i, j);
                            Position finale = EchecsUtil.getPosition(x, y);

                            byte ligne1 = EchecsUtil.indiceLigne(initiale);
                            byte ligne2 = EchecsUtil.indiceLigne(finale);
                            byte colonne1 = EchecsUtil.indiceColonne(initiale);
                            byte colonne2 = EchecsUtil.indiceColonne(finale);

                            Piece pieceADeplacer = echiquier[ligne1][colonne1];
                            Piece pieceFinale = echiquier[ligne2][colonne2];

                            // Vérifier si le mouvement est possible et le tester
                            if (deplace(initiale, finale)) {
                                changerTour();
                                // Si le joueur n'est plus en échec, le mouvement est possible
                                if (estEnEchec() != couleur) {
                                    if (pieceADeplacer instanceof Roi) {
                                        Roi roi = (Roi) pieceADeplacer;

                                        // Roque du côté roi
                                        if (colonne2 - colonne1 == 2 && ligne1 == ligne2) {

                                            echiquier[ligne2][colonne2] = pieceFinale; // Annuler le déplacement du roi
                                            echiquier[ligne1][colonne1] = roi;
                                            roi.setABouge(false);

                                            echiquier[ligne1][7] = echiquier[ligne2][colonne2 - 1]; // Annuler le déplacement de la tour
                                            echiquier[ligne2][colonne2 - 1] = null;
                                            ((Tour) echiquier[ligne2][colonne2 - 1]).setABouge(false);
                                        } else if (colonne2 - colonne1 == -2 && ligne1 == ligne2) { // Roque du coté dame
                                            echiquier[ligne2][colonne2] = pieceFinale; // Annuler le déplacement du roi
                                            echiquier[ligne1][colonne1] = roi;
                                            roi.setABouge(false);

                                            echiquier[ligne1][0] = echiquier[ligne2][colonne2 + 1]; // Annuler le déplacement de la tour
                                            echiquier[ligne2][colonne2 + 1] = null;
                                            ((Tour) echiquier[ligne2][colonne2 + 1]).setABouge(false);
                                        } else {
                                            // Annuler le mouvement pour ne pas modifier l'état du jeu
                                            echiquier[ligne1][colonne1] = roi;
                                            roi.setABouge(false);
                                            echiquier[ligne2][colonne2] = pieceFinale;
                                        }
                                    } else {
                                        // Annuler le mouvement pour ne pas modifier l'état du jeu
                                        echiquier[ligne1][colonne1] = pieceADeplacer;
                                        echiquier[ligne2][colonne2] = pieceFinale;
                                    }
                                    return false; // Le joueur peut éviter l'échec et mat
                                }
                                if (pieceADeplacer instanceof Roi) {
                                    Roi roi = (Roi) pieceADeplacer;

                                    // Roque du côté roi
                                    if (colonne2 - colonne1 == 2 && ligne1 == ligne2) {

                                        echiquier[ligne2][colonne2] = pieceFinale; // Annuler le déplacement du roi
                                        echiquier[ligne1][colonne1] = roi;
                                        roi.setABouge(false);

                                        echiquier[ligne1][7] = echiquier[ligne2][colonne2 - 1]; // Annuler le déplacement de la tour
                                        echiquier[ligne2][colonne2 - 1] = null;
                                        ((Tour) echiquier[ligne2][colonne2 - 1]).setABouge(false);
                                    } else if (colonne2 - colonne1 == -2 && ligne1 == ligne2) { // Roque du coté dame
                                        echiquier[ligne2][colonne2] = pieceFinale; // Annuler le déplacement du roi
                                        echiquier[ligne1][colonne1] = roi;
                                        roi.setABouge(false);

                                        echiquier[ligne1][0] = echiquier[ligne2][colonne2 + 1]; // Annuler le déplacement de la tour
                                        echiquier[ligne2][colonne2 + 1] = null;
                                        ((Tour) echiquier[ligne2][colonne2 + 1]).setABouge(false);
                                    } else {
                                        // Annuler le mouvement pour ne pas modifier l'état du jeu
                                        echiquier[ligne1][colonne1] = roi;
                                        roi.setABouge(false);
                                        echiquier[ligne2][colonne2] = pieceFinale;
                                    }
                                } else {
                                    // Annuler le mouvement pour ne pas modifier l'état du jeu
                                    echiquier[ligne1][colonne1] = pieceADeplacer;
                                    echiquier[ligne2][colonne2] = pieceFinale;
                                }
                            }
                        }
                    }
                }
            }
        }

        return true; // Aucun mouvement possible pour éviter l'échec et mat.
    }

    /**
     * Retourne la couleur n ou b du joueur qui a la main.
     *
     * @return char la couleur du joueur à qui c'est le tour de jouer.
     */
    public char getTour() {
        return tour;
    }
    /**
     * Retourne l'alias du premier joueur.
     * @return String alias du premier joueur.
     */
    public String getAliasJoueur1() {
        return aliasJoueur1;
    }
    /**
     * Modifie l'alias du premier joueur.
     * @param aliasJoueur1 String nouvel alias du premier joueur.
     */
    public void setAliasJoueur1(String aliasJoueur1) {
        this.aliasJoueur1 = aliasJoueur1;
    }
    /**
     * Retourne l'alias du deuxième joueur.
     * @return String alias du deuxième joueur.
     */
    public String getAliasJoueur2() {
        return aliasJoueur2;
    }
    /**
     * Modifie l'alias du deuxième joueur.
     * @param aliasJoueur2 String nouvel alias du deuxième joueur.
     */
    public void setAliasJoueur2(String aliasJoueur2) {
        this.aliasJoueur2 = aliasJoueur2;
    }
    /**
     * Retourne la couleur n ou b du premier joueur.
     * @return char couleur du premier joueur.
     */
    public char getCouleurJoueur1() {
        return couleurJoueur1;
    }
    /**
     * Retourne la couleur n ou b du deuxième joueur.
     * @return char couleur du deuxième joueur.
     */
    public char getCouleurJoueur2() {
        return couleurJoueur2;
    }

    public void setCouleurJoueur1(char couleurJoueur1) {
        this.couleurJoueur1 = couleurJoueur1;
    }

    public void setCouleurJoueur2(char couleurJoueur2) {
        this.couleurJoueur2 = couleurJoueur2;
    }

    public boolean getRoqueRoiVientEtreFait() {
        return roqueRoiVientEtreFait;
    }

    public void setRoqueRoiVientEtreFait(boolean roqueRoiVientEtreFait) {
        this.roqueRoiVientEtreFait = roqueRoiVientEtreFait;
    }

    public boolean getRoqueDameVientEtreFait() {
        return roqueDameVientEtreFait;
    }

    public void setRoqueDameVientEtreFait(boolean roqueDameVientEtreFait) {
        this.roqueDameVientEtreFait = roqueDameVientEtreFait;
    }
}