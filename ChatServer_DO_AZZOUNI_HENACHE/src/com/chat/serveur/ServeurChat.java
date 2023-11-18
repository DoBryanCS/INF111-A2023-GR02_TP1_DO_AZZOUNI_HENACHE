package com.chat.serveur;

import com.chat.commun.evenement.Evenement;
import com.chat.commun.net.Connexion;
import com.echecs.PartieEchecs;
import com.echecs.Position;

import java.util.Random;
import java.util.Vector;

/**
 * Cette classe étend (hérite) la classe abstraite Serveur et y ajoute le nécessaire pour que le
 * serveur soit un serveur de chat.
 *
 * @author Abdelmoumène Toudeft (Abdelmoumene.Toudeft@etsmtl.ca)
 * @version 1.0
 * @since 2023-09-15
 */
public class ServeurChat extends Serveur {

    //Liste des messages envoyés au salon de chat public :
    protected Vector<String> historique = new Vector<>();

    //Liste des invitations à un salon privé :
    protected Vector<Invitation> invitations = new Vector<>();

    //Liste des invitations à une partie d'échec :
    protected Vector<Invitation> invitationsEchec = new Vector<>();

    //Liste salons prives :
    protected Vector<SalonPrive> salonsPrives = new Vector<>();

    /**
     * Crée un serveur de chat qui va écouter sur le port spécifié.
     *
     * @param port int Port d'écoute du serveur
     */
    public ServeurChat(int port) {
        super(port);
    }

    @Override
    public synchronized boolean ajouter(Connexion connexion) {
        String hist = this.historique();
        if ("".equals(hist)) {
            connexion.envoyer("OK");
        }
        else {
            connexion.envoyer("HIST " + hist);
        }
        return super.ajouter(connexion);
    }
    /**
     * Valide l'arrivée d'un nouveau client sur le serveur. Cette redéfinition
     * de la méthode héritée de Serveur vérifie si le nouveau client a envoyé
     * un alias composé uniquement des caractères a-z, A-Z, 0-9, - et _.
     *
     * @param connexion Connexion la connexion représentant le client
     * @return boolean true, si le client a validé correctement son arrivée, false, sinon
     */
    @Override
    protected boolean validerConnexion(Connexion connexion) {

        String texte = connexion.getAvailableText().trim();
        char c;
        int taille;
        boolean res = true;
        if ("".equals(texte)) {
            return false;
        }
        taille = texte.length();
        for (int i=0;i<taille;i++) {
            c = texte.charAt(i);
            if ((c<'a' || c>'z') && (c<'A' || c>'Z') && (c<'0' || c>'9')
                    && c!='_' && c!='-') {
                res = false;
                break;
            }
        }
        if (!res)
            return false;
        for (Connexion cnx:connectes) {
            if (texte.equalsIgnoreCase(cnx.getAlias())) { //alias déjà utilisé
                res = false;
                break;
            }
        }
        connexion.setAlias(texte);
        return true;
    }

    /**
     * Retourne la liste des alias des connectés au serveur dans une chaîne de caractères.
     *
     * @return String chaîne de caractères contenant la liste des alias des membres connectés sous la
     * forme alias1:alias2:alias3 ...
     */
    public String list() {
        String s = "";
        for (Connexion cnx:connectes)
            s+=cnx.getAlias()+":";
        return s;
    }

    /**
     * Retourne la liste des messages de l'historique de chat dans une chaîne
     * de caractères.
     *
     * @return String chaîne de caractères contenant la liste des alias des membres connectés sous la
     * forme message1\nmessage2\nmessage3 ...
     */
    public String historique() {
        String s = "";
        for (String str: historique)
            s+= str + "\n";
        return s;
    }

    /**
     * Retourne la liste des alias qui ont envoyés une invitation à utilisateur.
     *
     * @param alias String chaîne de caractères représentant l'alias d'utilisateur invité dans une invitation.
     *
     * @return String chaîne de caractères contenant la liste des alias sous la forme alias1:alias2:alias3 ...
     */
    public String listInvitations(String alias) {
        String s = "";
        for (Invitation invitation:invitations) {
            if (invitation.getAliasInvite().equals(alias))
                s += invitation.getAliasHote() + ":";
        }
        return s;
    }

    /**
     * Envoie la chaîne str à tous les utilisateurs connectés sauf à celui
     * qui a l’alias aliasExpediteur.
     *
     * @param str String chaine de caractères représentant le message à envoyer
     * @param aliasExpediteur String chaine de caractères représentant l'alias d'un utilisateur connecté
     */
    public void envoyerATousSauf(String str, String aliasExpediteur) {
        String message = aliasExpediteur + ">>" + str;
        ajouterHistorique(message);
        for (Connexion cnx:connectes) {
            if (!cnx.getAlias().equals(aliasExpediteur))
                cnx.envoyer(message);
        }
    }

    /**
     * Reçoit une chaîne de caractères (qui représente un message) et
     * l’ajoute à l’historique des messages.
     *
     * @param str String chaine de caractères représentant le message à ajouter
     */
    public void ajouterHistorique(String str) {
        historique.add(str);
    }

    /**
     * Envoie la chaîne message à seulement un utilisateur qui a l'alias alias en privé
     *
     * @param alias String chaine de caractères représentant le message à envoyer
     *
     * @return Connexion représentant l'object Connexion d'un utilisateur
     */
    public Connexion envoyerMessagePrive(String alias) {
        for (Connexion utilisateur : connectes) {
            if (utilisateur.getAlias().equals(alias)) {
                return utilisateur;
            }
        }
        return null;
    }

    /**
     * Vérifie l'existence de l'alias en tant qu'utilisateur
     *
     * @param alias String chaine de caractères représentant l'alias d'un utilisateur
     *
     * @return Boolean représentant l'existence de l'utilisateur ou non
     */
    public boolean verifierExistenceUtilisateur(String alias) {
        for (Connexion utilisateur : connectes) {
            if (utilisateur.getAlias().equals(alias)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Vérifie l'existence d'un salon privé avec deux utilisateurs
     *
     * @param alias1 String chaine de caractères représentant l'alias d'un utilisateur
     * @param alias2 String chaine de caractères représentant l'alias d'un utilisateur
     *
     * @return int représentant l'index du salon privé ou -1 si il n'existe pas
     */
    public int verifierExistenceSalonPrive(String alias1, String alias2) {
        for (SalonPrive salonPrive:salonsPrives) {
            if (salonPrive.equals(new SalonPrive(alias1, alias2)))
                return salonsPrives.indexOf(salonPrive);
        }

        return -1;
    }

    /**
     * Trouver le salon privé en partie d'échec avec un utilisateur
     *
     * @param alias1 String chaine de caractères représentant l'alias d'un utilisateur
     *
     * @return int représentant l'index du salon privé ou -1 si il n'existe pas
     */
    public int rechercheSalonPrive(String alias1) {
        for (SalonPrive salonPrive:salonsPrives) {
            if ((salonPrive.getAliasHote().equals(alias1) || salonPrive.getAliasInvite().equals(alias1)) && salonPrive.getPartieEchecs() != null)
                return salonsPrives.indexOf(salonPrive);
        }

        return -1;
    }

    /**
     * Vérifie l'existence d'une invitation de chat privé entre deux utilisateurs
     *
     * @param alias1 String chaine de caractères représentant l'alias d'un utilisateur
     * @param alias2 String chaine de caractères représentant l'alias d'un utilisateur
     *
     * @return Invitation représentant l'objet invitation ou null
     */
    public Invitation verifierExistenceInvitation(String alias1, String alias2, boolean isInvitationEchec) {
        Invitation invitation = new Invitation(alias1, alias2, isInvitationEchec);
        if (!isInvitationEchec) {
            if (invitations.contains(invitation))
                return invitation;
        } else {
            if (invitationsEchec.contains(invitation))
                return invitation;
        }

        return null;
    }

    /**
     * Gère la commande JOIN lorsqu'une invitation existe
     *
     * @param cnx Connexion objet qui représente l'utilisateur qui envoit la commande
     * @param aliasInvite String chaine de caractères représentant l'alias de l'expéditeur
     * @param aliasExpediteur String chaine de caractères représentant l'alias de l'invité
     * @param invitation Invitation Objet représentant l'invitation
     */
    public void traiterJoinInvitationExistente(Connexion cnx, String aliasExpediteur, String aliasInvite, Invitation invitation) {
        boolean isAliasHote = invitation.getIsAliasHote();
        boolean isAliasInvite = invitation.getIsAliasInvite();
        boolean isInvitationEchec = invitation.getIsInvitationEchec();

        if (isAliasHote) {
            cnx.envoyer("Vous avez déjà envoyé une invitation à " + aliasInvite + "!");
        } else if (isAliasInvite) {
            if (!isInvitationEchec) {
                salonsPrives.add(new SalonPrive(aliasInvite, aliasExpediteur));
                invitations.remove(new Invitation(aliasInvite, aliasExpediteur, false));

                envoyerMessagePrive(aliasInvite).envoyer("JOINOK " + aliasExpediteur);
                envoyerMessagePrive(aliasExpediteur).envoyer("JOINOK " + aliasInvite);
            } else {
                for (Invitation invitationEchec : invitationsEchec) {
                    if(invitationEchec.getAliasHote().equals(aliasExpediteur))
                        invitationsEchec.remove(invitationEchec);
                }

                Random random = new Random();
                char couleurAleatoire = random.nextBoolean() ? 'b' : 'n';

                int indexSalonPrive = verifierExistenceSalonPrive(aliasInvite, aliasExpediteur);

                PartieEchecs partieEchecs = new PartieEchecs();
                partieEchecs.setAliasJoueur1(aliasInvite);
                partieEchecs.setAliasJoueur2(aliasExpediteur);
                partieEchecs.setCouleurJoueur1(couleurAleatoire);
                partieEchecs.setCouleurJoueur2(couleurAleatoire == 'b' ? 'n' : 'b');
                salonsPrives.get(indexSalonPrive).setPartieEchecs(partieEchecs);

                invitationsEchec.remove(new Invitation(aliasInvite, aliasExpediteur, true));
                envoyerMessagePrive(aliasInvite).envoyer("CHESSOK " + couleurAleatoire);
                envoyerMessagePrive(aliasExpediteur).envoyer("CHESSOK " + (couleurAleatoire == 'b' ? 'n' : 'b'));
            }
        }
    }

    /**
     * Gère la commande DECLINE pour une invitation
     *
     * @param cnx             Connexion objet qui représente l'utilisateur qui envoit la commande
     * @param aliasExpediteur String chaine de caractères représentant l'alias de l'expéditeur
     * @param aliasInvite     String chaine de caractères représentant l'alias de l'invité
     * @param invitation      Invitation Objet représentant l'invitation
     */
    public void traiterDeclineInvitation(Connexion cnx, String aliasExpediteur, String aliasInvite, Invitation invitation) {
        boolean isInvitationEchec = invitation.getIsInvitationEchec();

        if (!isInvitationEchec) {
            invitations.remove(invitation);
        } else
            invitationsEchec.remove(invitation);

        boolean isAliasHote = invitation.getIsAliasHote();
        boolean isAliasInvite = invitation.getIsAliasInvite();

        if (isAliasHote) {
            cnx.envoyer("Annulation de l'invitation avec succès!");
        } else if (isAliasInvite) {
            cnx.envoyer("Refus de l'invitation avec succès!");
            envoyerMessagePrive(aliasInvite).envoyer("DECLINE " + aliasExpediteur);
        }
    }

    /**
     * Gère la commande MOVE pour un mouvement dans une partie d'échec
     *
     * @param cnx             Connexion objet qui représente l'utilisateur qui envoit la commande
     * @param evenement       Evenement objet représentant la commande
     */
    public void traiterMouvement(Evenement evenement, Connexion cnx) {
        String aliasExpediteur = cnx.getAlias();
        String deplacement = evenement.getArgument();
        int indexSalonPrive = rechercheSalonPrive(aliasExpediteur);

        if (indexSalonPrive != -1 && salonsPrives.get(indexSalonPrive).getPartieEchecs() != null) {
            PartieEchecs partieEchecs = salonsPrives.get(indexSalonPrive).getPartieEchecs();
            char couleurJoueurActuel = aliasExpediteur.equals(partieEchecs.getAliasJoueur1()) ?
                    partieEchecs.getCouleurJoueur1() : partieEchecs.getCouleurJoueur2();
            String aliasJoueurEnnemi = aliasExpediteur.equals(partieEchecs.getAliasJoueur1()) ?
                    partieEchecs.getAliasJoueur2() : partieEchecs.getAliasJoueur1();
            char couleurJoueurEnnemi = couleurJoueurActuel == 'b' ? 'n' : 'b';

            if (partieEchecs.getTour() != couleurJoueurActuel) {
                cnx.envoyer("Ce n'est pas votre tour de jouer!");
            } else {
                deplacement = deplacement.replaceAll("[\\s-]", "");
                char[] chars = deplacement.toCharArray();
                String digits = "";

                for (char c : chars) {
                    if (Character.isDigit(c)) {
                        digits += c;
                    }
                }

                // Vérification du format
                if (chars.length == 4 && digits.length() == 2) {
                    traiterMouvementValide(chars, digits, deplacement, partieEchecs, cnx, aliasExpediteur, aliasJoueurEnnemi, couleurJoueurEnnemi);
                } else {
                    cnx.envoyer("INVALID");
                }
            }
        } else {
            cnx.envoyer("Vous n'êtes pas dans une partie d'échec!");
        }
    }

    /**
     * Gère une commande MOVE qui est valide pour un mouvement dans une partie d'échec
     *
     * @param chars           Tableau des caractères représentant le mouvement
     * @param digits          Chaine de caractères représentant les lignes d'un mouvement
     * @param deplacement     Chaine de caractères représentant le mouvement
     * @param partieEchecs    PartieEchecs objet représentant une partie d'échec
     * @param cnx             Connexion objet qui représente l'utilisateur qui envoit la commande
     * @param aliasExpediteur String chaine de caractères représentant l'alias de l'expéditeur
     * @param aliasJoueurEnnemi String chaine de caractères représentant le joueur adverse
     * @param couleurJoueurEnnemi char caractère représentant la couleur du joueur adverse
     */
    private void traiterMouvementValide(char[] chars, String digits,  String deplacement, PartieEchecs partieEchecs, Connexion cnx, String aliasExpediteur, String aliasJoueurEnnemi, char couleurJoueurEnnemi) {
        char colonne1 = chars[0];
        byte ligne1 = Byte.parseByte(digits.substring(0, 1));
        char colonne2 = chars[2];
        byte ligne2 = Byte.parseByte(digits.substring(1, 2));

        String deplacementRoqueRoi = "h" + ligne1 + "f" + ligne1;
        String deplacementRoqueDame = "a" + ligne1 + "d" + ligne1;

        if (partieEchecs.deplace(new Position(colonne1, ligne1), new Position(colonne2, ligne2))) {
            boolean roqueRoiFait = partieEchecs.getRoqueRoiVientEtreFait();
            boolean roqueDameFait = partieEchecs.getRoqueDameVientEtreFait();
            boolean estEnEchec = partieEchecs.estEnEchec() == couleurJoueurEnnemi;

            if (!partieEchecs.estEnEchecEtMat(partieEchecs.getTour())) {
                traiterNonEchecEtMat(roqueRoiFait, roqueDameFait, deplacementRoqueRoi, deplacementRoqueDame, estEnEchec, deplacement, cnx, aliasJoueurEnnemi, aliasExpediteur);
            } else {
                traiterEchecEtMat(roqueRoiFait, roqueDameFait, deplacementRoqueRoi, deplacementRoqueDame, deplacement, cnx, aliasExpediteur, aliasJoueurEnnemi, partieEchecs);
            }
        } else {
            cnx.envoyer("INVALID");
        }
    }

    private void traiterNonEchecEtMat(boolean roqueRoiFait, boolean roqueDameFait, String deplacementRoqueRoi, String deplacementRoqueDame,  boolean estEnEchec, String deplacement, Connexion cnx, String aliasJoueurEnnemi, String aliasExpediteur) {
        int indexSalonPrive = rechercheSalonPrive(aliasExpediteur);
        PartieEchecs partieEchecs = salonsPrives.get(indexSalonPrive).getPartieEchecs();

        if ((roqueRoiFait || roqueDameFait) && !estEnEchec) {
            String deplacementRoque = roqueRoiFait ? deplacementRoqueRoi : deplacementRoqueDame;
            cnx.envoyer("MOVE " + deplacement + deplacementRoque);
            envoyerMessagePrive(aliasJoueurEnnemi).envoyer("MOVE " + deplacement + deplacementRoque);
            partieEchecs.setRoqueRoiVientEtreFait(false);
            partieEchecs.setRoqueDameVientEtreFait(false);
        } else if ((!roqueRoiFait && !roqueDameFait) && !estEnEchec) {
            cnx.envoyer("MOVE " + deplacement);
            envoyerMessagePrive(aliasJoueurEnnemi).envoyer("MOVE " + deplacement);
        } else if (roqueRoiFait || roqueDameFait) {
            String deplacementRoque = roqueRoiFait ? deplacementRoqueRoi : deplacementRoqueDame;
            String moveMessage = "ECHEC " + aliasJoueurEnnemi + "/" + deplacement + deplacementRoque;
            cnx.envoyer(moveMessage);
            envoyerMessagePrive(aliasJoueurEnnemi).envoyer(moveMessage);
            partieEchecs.setRoqueRoiVientEtreFait(false);
            partieEchecs.setRoqueDameVientEtreFait(false);
        } else {
            String moveMessage = "ECHEC " + aliasJoueurEnnemi + "/" + deplacement;
            cnx.envoyer(moveMessage);
            envoyerMessagePrive(aliasJoueurEnnemi).envoyer(moveMessage);
        }
    }

    private void traiterEchecEtMat(boolean roqueRoiFait, boolean roqueDameFait, String deplacementRoqueRoi, String deplacementRoqueDame, String deplacement, Connexion cnx, String aliasExpediteur, String aliasJoueurEnnemi, PartieEchecs partieEchecs) {
        int indexSalonPrive = rechercheSalonPrive(aliasExpediteur);
        if (roqueRoiFait || roqueDameFait) {
            String deplacementRoque = roqueRoiFait ? deplacementRoqueRoi : deplacementRoqueDame;
            String moveMessage = "MAT " + aliasExpediteur + "/" + deplacement + deplacementRoque;
            cnx.envoyer(moveMessage);
            envoyerMessagePrive(aliasJoueurEnnemi).envoyer(moveMessage);
            partieEchecs.setRoqueRoiVientEtreFait(false);
            partieEchecs.setRoqueDameVientEtreFait(false);
        } else {
            cnx.envoyer("MAT " + aliasExpediteur + "/" + deplacement);
            envoyerMessagePrive(aliasJoueurEnnemi).envoyer("MAT " + aliasExpediteur + "/" + deplacement);
            salonsPrives.get(indexSalonPrive).setPartieEchecs(null);
        }
    }
}
