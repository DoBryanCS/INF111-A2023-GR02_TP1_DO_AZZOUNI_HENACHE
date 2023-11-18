package com.chat.serveur;

import com.chat.commun.evenement.Evenement;
import com.chat.commun.net.Connexion;
import com.echecs.PartieEchecs;
import com.echecs.Position;

import java.util.Random;
import java.util.Vector;

/**
 * Cette classe �tend (h�rite) la classe abstraite Serveur et y ajoute le n�cessaire pour que le
 * serveur soit un serveur de chat.
 *
 * @author Abdelmoum�ne Toudeft (Abdelmoumene.Toudeft@etsmtl.ca)
 * @version 1.0
 * @since 2023-09-15
 */
public class ServeurChat extends Serveur {

    //Liste des messages envoy�s au salon de chat public :
    protected Vector<String> historique = new Vector<>();

    //Liste des invitations � un salon priv� :
    protected Vector<Invitation> invitations = new Vector<>();

    //Liste des invitations � une partie d'�chec :
    protected Vector<Invitation> invitationsEchec = new Vector<>();

    //Liste salons prives :
    protected Vector<SalonPrive> salonsPrives = new Vector<>();

    /**
     * Cr�e un serveur de chat qui va �couter sur le port sp�cifi�.
     *
     * @param port int Port d'�coute du serveur
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
     * Valide l'arriv�e d'un nouveau client sur le serveur. Cette red�finition
     * de la m�thode h�rit�e de Serveur v�rifie si le nouveau client a envoy�
     * un alias compos� uniquement des caract�res a-z, A-Z, 0-9, - et _.
     *
     * @param connexion Connexion la connexion repr�sentant le client
     * @return boolean true, si le client a valid� correctement son arriv�e, false, sinon
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
            if (texte.equalsIgnoreCase(cnx.getAlias())) { //alias d�j� utilis�
                res = false;
                break;
            }
        }
        connexion.setAlias(texte);
        return true;
    }

    /**
     * Retourne la liste des alias des connect�s au serveur dans une cha�ne de caract�res.
     *
     * @return String cha�ne de caract�res contenant la liste des alias des membres connect�s sous la
     * forme alias1:alias2:alias3 ...
     */
    public String list() {
        String s = "";
        for (Connexion cnx:connectes)
            s+=cnx.getAlias()+":";
        return s;
    }

    /**
     * Retourne la liste des messages de l'historique de chat dans une cha�ne
     * de caract�res.
     *
     * @return String cha�ne de caract�res contenant la liste des alias des membres connect�s sous la
     * forme message1\nmessage2\nmessage3 ...
     */
    public String historique() {
        String s = "";
        for (String str: historique)
            s+= str + "\n";
        return s;
    }

    /**
     * Retourne la liste des alias qui ont envoy�s une invitation � utilisateur.
     *
     * @param alias String cha�ne de caract�res repr�sentant l'alias d'utilisateur invit� dans une invitation.
     *
     * @return String cha�ne de caract�res contenant la liste des alias sous la forme alias1:alias2:alias3 ...
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
     * Envoie la cha�ne str � tous les utilisateurs connect�s sauf � celui
     * qui a l�alias aliasExpediteur.
     *
     * @param str String chaine de caract�res repr�sentant le message � envoyer
     * @param aliasExpediteur String chaine de caract�res repr�sentant l'alias d'un utilisateur connect�
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
     * Re�oit une cha�ne de caract�res (qui repr�sente un message) et
     * l�ajoute � l�historique des messages.
     *
     * @param str String chaine de caract�res repr�sentant le message � ajouter
     */
    public void ajouterHistorique(String str) {
        historique.add(str);
    }

    /**
     * Envoie la cha�ne message � seulement un utilisateur qui a l'alias alias en priv�
     *
     * @param alias String chaine de caract�res repr�sentant le message � envoyer
     *
     * @return Connexion repr�sentant l'object Connexion d'un utilisateur
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
     * V�rifie l'existence de l'alias en tant qu'utilisateur
     *
     * @param alias String chaine de caract�res repr�sentant l'alias d'un utilisateur
     *
     * @return Boolean repr�sentant l'existence de l'utilisateur ou non
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
     * V�rifie l'existence d'un salon priv� avec deux utilisateurs
     *
     * @param alias1 String chaine de caract�res repr�sentant l'alias d'un utilisateur
     * @param alias2 String chaine de caract�res repr�sentant l'alias d'un utilisateur
     *
     * @return int repr�sentant l'index du salon priv� ou -1 si il n'existe pas
     */
    public int verifierExistenceSalonPrive(String alias1, String alias2) {
        for (SalonPrive salonPrive:salonsPrives) {
            if (salonPrive.equals(new SalonPrive(alias1, alias2)))
                return salonsPrives.indexOf(salonPrive);
        }

        return -1;
    }

    /**
     * Trouver le salon priv� en partie d'�chec avec un utilisateur
     *
     * @param alias1 String chaine de caract�res repr�sentant l'alias d'un utilisateur
     *
     * @return int repr�sentant l'index du salon priv� ou -1 si il n'existe pas
     */
    public int rechercheSalonPrive(String alias1) {
        for (SalonPrive salonPrive:salonsPrives) {
            if ((salonPrive.getAliasHote().equals(alias1) || salonPrive.getAliasInvite().equals(alias1)) && salonPrive.getPartieEchecs() != null)
                return salonsPrives.indexOf(salonPrive);
        }

        return -1;
    }

    /**
     * V�rifie l'existence d'une invitation de chat priv� entre deux utilisateurs
     *
     * @param alias1 String chaine de caract�res repr�sentant l'alias d'un utilisateur
     * @param alias2 String chaine de caract�res repr�sentant l'alias d'un utilisateur
     *
     * @return Invitation repr�sentant l'objet invitation ou null
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
     * G�re la commande JOIN lorsqu'une invitation existe
     *
     * @param cnx Connexion objet qui repr�sente l'utilisateur qui envoit la commande
     * @param aliasInvite String chaine de caract�res repr�sentant l'alias de l'exp�diteur
     * @param aliasExpediteur String chaine de caract�res repr�sentant l'alias de l'invit�
     * @param invitation Invitation Objet repr�sentant l'invitation
     */
    public void traiterJoinInvitationExistente(Connexion cnx, String aliasExpediteur, String aliasInvite, Invitation invitation) {
        boolean isAliasHote = invitation.getIsAliasHote();
        boolean isAliasInvite = invitation.getIsAliasInvite();
        boolean isInvitationEchec = invitation.getIsInvitationEchec();

        if (isAliasHote) {
            cnx.envoyer("Vous avez d�j� envoy� une invitation � " + aliasInvite + "!");
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
     * G�re la commande DECLINE pour une invitation
     *
     * @param cnx             Connexion objet qui repr�sente l'utilisateur qui envoit la commande
     * @param aliasExpediteur String chaine de caract�res repr�sentant l'alias de l'exp�diteur
     * @param aliasInvite     String chaine de caract�res repr�sentant l'alias de l'invit�
     * @param invitation      Invitation Objet repr�sentant l'invitation
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
            cnx.envoyer("Annulation de l'invitation avec succ�s!");
        } else if (isAliasInvite) {
            cnx.envoyer("Refus de l'invitation avec succ�s!");
            envoyerMessagePrive(aliasInvite).envoyer("DECLINE " + aliasExpediteur);
        }
    }

    /**
     * G�re la commande MOVE pour un mouvement dans une partie d'�chec
     *
     * @param cnx             Connexion objet qui repr�sente l'utilisateur qui envoit la commande
     * @param evenement       Evenement objet repr�sentant la commande
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

                // V�rification du format
                if (chars.length == 4 && digits.length() == 2) {
                    traiterMouvementValide(chars, digits, deplacement, partieEchecs, cnx, aliasExpediteur, aliasJoueurEnnemi, couleurJoueurEnnemi);
                } else {
                    cnx.envoyer("INVALID");
                }
            }
        } else {
            cnx.envoyer("Vous n'�tes pas dans une partie d'�chec!");
        }
    }

    /**
     * G�re une commande MOVE qui est valide pour un mouvement dans une partie d'�chec
     *
     * @param chars           Tableau des caract�res repr�sentant le mouvement
     * @param digits          Chaine de caract�res repr�sentant les lignes d'un mouvement
     * @param deplacement     Chaine de caract�res repr�sentant le mouvement
     * @param partieEchecs    PartieEchecs objet repr�sentant une partie d'�chec
     * @param cnx             Connexion objet qui repr�sente l'utilisateur qui envoit la commande
     * @param aliasExpediteur String chaine de caract�res repr�sentant l'alias de l'exp�diteur
     * @param aliasJoueurEnnemi String chaine de caract�res repr�sentant le joueur adverse
     * @param couleurJoueurEnnemi char caract�re repr�sentant la couleur du joueur adverse
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
