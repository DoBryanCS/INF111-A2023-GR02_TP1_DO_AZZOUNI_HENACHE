package com.chat.client;

/**
 * Cette classe étend la classe Client pour lui ajouter des fonctionnalités
 * spécifiques au chat et au jeu d'échecs en réseau.
 *
 * @author Abdelmoumène Toudeft (Abdelmoumene.Toudeft@etsmtl.ca)
 * @version 1.0
 * @since 2023-09-01
 */
public class ClientChat extends Client {
    private EtatPartieEchecs etatPartieEchecs;

    public EtatPartieEchecs getEtatPartieEchecs() {
        return etatPartieEchecs;
    }

    public void setEtatPartieEchecs(EtatPartieEchecs etatPartieEchecs) {
        this.etatPartieEchecs = etatPartieEchecs;
    }

    public void nouvellePartie() {
        etatPartieEchecs = new EtatPartieEchecs();
    }

    public void gererMouvement(String argument, Object client) {
        String arg = argument.replaceAll("[\\s-]", "");
        char[] chars = arg.toCharArray();
        String digits = "";

        for (char c : chars) {
            if (Character.isDigit(c)) {
                digits += c;
            }
        }

        if (chars.length >= 4) {
            char colonne1 = chars[0];
            byte ligne1 = Byte.parseByte(digits.substring(0, 1));
            char colonne2 = chars[2];
            byte ligne2 = Byte.parseByte(digits.substring(1, 2));

            ClientChat clientChat = (ClientChat) client;
            EtatPartieEchecs etatPartieEchecs = clientChat.getEtatPartieEchecs();
            char[][] nouvelEtat = etatPartieEchecs.getEtatEchiquier();

            char pieceDeplacee = nouvelEtat[8 - ligne1][colonne1 - 'a'];
            nouvelEtat[8 - ligne1][colonne1 - 'a'] = ' ';
            nouvelEtat[8 - ligne2][colonne2 - 'a'] = pieceDeplacee;

            if (ligne2 == 8 && pieceDeplacee == 'P') {
                nouvelEtat[8 - ligne2][colonne2 - 'a'] = 'D';
            }

            if (ligne2 == 1 && pieceDeplacee == 'p') {
                nouvelEtat[8 - ligne2][colonne2 - 'a'] = 'd';
            }

            etatPartieEchecs.setEtatEchiquier(nouvelEtat);
            clientChat.setEtatPartieEchecs(etatPartieEchecs);

            System.out.println(etatPartieEchecs);
            System.out.println();

            if (chars.length >= 8 && digits.length() >= 4) {
                char colonne3 = chars[4];
                byte ligne3 = Byte.parseByte(digits.substring(2, 3));
                char colonne4 = chars[6];
                byte ligne4 = Byte.parseByte(digits.substring(3, 4));

                etatPartieEchecs = clientChat.getEtatPartieEchecs();
                nouvelEtat = etatPartieEchecs.getEtatEchiquier();

                pieceDeplacee = nouvelEtat[8 - ligne3][colonne3 - 'a'];
                nouvelEtat[8 - ligne3][colonne3 - 'a'] = ' ';
                nouvelEtat[8 - ligne4][colonne4 - 'a'] = pieceDeplacee;

                etatPartieEchecs.setEtatEchiquier(nouvelEtat);
                clientChat.setEtatPartieEchecs(etatPartieEchecs);

                System.out.println(etatPartieEchecs);
                System.out.println();
            }
        }
    }
 }
