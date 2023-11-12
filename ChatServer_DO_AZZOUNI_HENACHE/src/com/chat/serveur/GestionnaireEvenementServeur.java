package com.chat.serveur;

import com.chat.commun.evenement.Evenement;
import com.chat.commun.evenement.EvenementUtil;
import com.chat.commun.evenement.GestionnaireEvenement;
import com.chat.commun.net.Connexion;
import com.echecs.PartieEchecs;
import com.echecs.Position;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cette classe repr�sente un gestionnaire d'�v�nement d'un serveur. Lorsqu'un serveur re�oit un texte d'un client,
 * il cr�e un �v�nement � partir du texte re�u et alerte ce gestionnaire qui r�agit en g�rant l'�v�nement.
 *
 * @author Abdelmoum�ne Toudeft (Abdelmoumene.Toudeft@etsmtl.ca)
 * @version 1.0
 * @since 2023-09-01
 */
public class GestionnaireEvenementServeur implements GestionnaireEvenement {
    private Serveur serveur;

    /**
     * Construit un gestionnaire d'�v�nements pour un serveur.
     *
     * @param serveur Serveur Le serveur pour lequel ce gestionnaire g�re des �v�nements
     */
    public GestionnaireEvenementServeur(Serveur serveur) {
        this.serveur = serveur;
    }

    /**
     * M�thode de gestion d'�v�nements. Cette m�thode contiendra le code qui g�re les r�ponses obtenues d'un client.
     *
     * @param evenement L'�v�nement � g�rer.
     */
    @Override
    public void traiter(Evenement evenement) {
        Object source = evenement.getSource();
        Connexion cnx;
        String msg, typeEvenement, aliasExpediteur, aliasInvite, listInvitations, deplacement;
        boolean utilisateurExiste, invitationSoi;
        Invitation invitation;
        int indexSalonPrive;
        PartieEchecs partieEchecs;
        ServeurChat serveur = (ServeurChat) this.serveur;

        if (source instanceof Connexion) {
            cnx = (Connexion) source;
            System.out.println("SERVEUR-Recu : " + evenement.getType() + " " + evenement.getArgument());
            typeEvenement = evenement.getType();
            switch (typeEvenement) {
                case "EXIT": //Ferme la connexion avec le client qui a envoy� "EXIT":
                    cnx.envoyer("END");
                    serveur.enlever(cnx);
                    cnx.close();
                    break;
                case "LIST": //Envoie la liste des alias des personnes connect�es :
                    cnx.envoyer("LIST " + serveur.list());
                    break;
                case "MSG": //Envoie un message � tous les utilisateurs connect�s sauf l'exp�diteur :
                    aliasExpediteur = cnx.getAlias();
                    msg = evenement.getArgument();
                    serveur.envoyerATousSauf(msg, aliasExpediteur);
                    break;
                case "HIST": //Affiche l'historique des messages :
                    cnx.envoyer("HIST " + serveur.historique());
                    break;
                case "JOIN": //Invite un utilisateur � chatter en priv� ou accepte l�invitation qui lui a �t�
                    //pr�alablement envoy�e par un utilisateur :
                    aliasExpediteur = cnx.getAlias();
                    aliasInvite = evenement.getArgument();

                    indexSalonPrive = serveur.verifierExistenceSalonPrive(aliasExpediteur, aliasInvite);
                    utilisateurExiste = serveur.verifierExistenceUtilisateur(aliasInvite);
                    invitationSoi = aliasExpediteur.equals(aliasInvite);

                    if (utilisateurExiste && !invitationSoi && indexSalonPrive == -1) {
                        invitation = serveur.verifierExistenceInvitation(aliasExpediteur, aliasInvite, false);

                        if (invitation != null) {
                            serveur.traiterJoinInvitationExistente(cnx, aliasExpediteur, aliasInvite, invitation);
                        } else {
                            serveur.invitations.add(new Invitation(aliasExpediteur, aliasInvite, false));
                            cnx.envoyer("Succ�s de l'invitation!");
                            serveur.envoyerMessagePrive(aliasInvite).envoyer("JOIN " + aliasExpediteur);
                        }
                    }

                    if (indexSalonPrive != -1)
                        cnx.envoyer("Le salon priv� avec " + aliasInvite + " existe d�ja!");

                    if (!utilisateurExiste)
                        cnx.envoyer("L'utilisateur " + aliasInvite + " n'existe pas!");

                    if (invitationSoi)
                        cnx.envoyer("Vous ne pouvez pas vous inviter vous-m�me!");
                    break;
                case "DECLINE": //Refuse une invitation � chatter en priv� d'un utilisateur ou annule une invitation
                    //qu'il a pr�alablement envoy�e � un utilisateur :
                    aliasExpediteur = cnx.getAlias();
                    aliasInvite = evenement.getArgument();

                    indexSalonPrive = serveur.verifierExistenceSalonPrive(aliasExpediteur, aliasInvite);
                    utilisateurExiste = serveur.verifierExistenceUtilisateur(aliasInvite);
                    invitationSoi = aliasExpediteur.equals(aliasInvite);

                    if (utilisateurExiste && !invitationSoi) {
                        if (indexSalonPrive == -1) {
                            invitation = serveur.verifierExistenceInvitation(aliasExpediteur, aliasInvite, false);

                            if (invitation != null) {
                                serveur.traiterDeclineInvitation(cnx, aliasExpediteur, aliasInvite, invitation);
                            } else {
                                cnx.envoyer("Aucune invitation � refuser ou � annuler en lien avec l'utilisateur "
                                        + aliasInvite + "!");
                            }
                        } else {
                            invitation = serveur.verifierExistenceInvitation(aliasExpediteur, aliasInvite, true);

                            if (invitation != null) {
                                serveur.traiterDeclineInvitation(cnx, aliasExpediteur, aliasInvite, invitation);
                            } else {
                                cnx.envoyer("Aucune invitation � refuser ou � annuler en lien avec l'utilisateur "
                                        + aliasInvite + "!");
                            }
                        }
                    }

                    if (!utilisateurExiste)
                        cnx.envoyer("L'utilisateur " + aliasInvite + " n'existe pas!");

                    if (invitationSoi)
                        cnx.envoyer("Vous ne pouvez pas vous refuser ou annuler une invitation � vous-m�me!");
                    break;
                case "INV": //Obtenir la liste de tous les alias des personnes qui lui ont envoy� des invitations :
                    aliasExpediteur = cnx.getAlias();
                    listInvitations = serveur.listInvitations(aliasExpediteur);
                    cnx.envoyer("INV " + listInvitations);
                    break;
                case "PRV": //Envoyer un message � un utilisateur dans un salon priv� :
                    aliasExpediteur = cnx.getAlias();
                    String aliasMessage = evenement.getArgument();
                    String[] t;

                    t = EvenementUtil.extraireInfosEvenement(aliasMessage);
                    aliasInvite = t[0];
                    msg = aliasExpediteur + ">>" + t[1];

                    indexSalonPrive = serveur.verifierExistenceSalonPrive(aliasExpediteur, aliasInvite);

                    if (indexSalonPrive != -1)
                        serveur.envoyerMessagePrive(aliasInvite).envoyer(msg);
                    else
                        cnx.envoyer("Le salon priv� avec " + aliasInvite + " n'existe pas!");
                    break;
                case "QUIT": //Quitter le salon priv� avec un utilisateur :
                    aliasExpediteur = cnx.getAlias();
                    aliasInvite = evenement.getArgument();

                    indexSalonPrive = serveur.verifierExistenceSalonPrive(aliasExpediteur, aliasInvite);

                    if (indexSalonPrive != -1) {
                        serveur.salonsPrives.remove(new SalonPrive(aliasExpediteur, aliasInvite));
                        cnx.envoyer("Vous avez quitt� le salon priv� avec succ�s!");
                        serveur.envoyerMessagePrive(aliasInvite).envoyer("QUIT " + aliasExpediteur);
                    } else
                        cnx.envoyer("Le salon priv� avec " + aliasInvite + " n'existe pas!");
                    break;
                case "CHESS":
                    aliasExpediteur = cnx.getAlias();
                    aliasInvite = evenement.getArgument();

                    indexSalonPrive = serveur.verifierExistenceSalonPrive(aliasExpediteur, aliasInvite);

                    invitation = serveur.verifierExistenceInvitation(aliasExpediteur, aliasInvite, true);

                        if (indexSalonPrive != -1) {
                            if (serveur.salonsPrives.get(indexSalonPrive).getPartieEchecs() == null) {
                                if (invitation != null)
                                    serveur.traiterJoinInvitationExistente(cnx, aliasExpediteur, aliasInvite, invitation);
                                else {
                                    serveur.invitationsEchec.add(new Invitation(aliasExpediteur, aliasInvite, true));
                                    cnx.envoyer("Succ�s de l'invitation!");
                                    serveur.envoyerMessagePrive(aliasInvite).envoyer("CHESS " + aliasExpediteur);
                                }
                            } else
                                cnx.envoyer("Vous �tes d�j� en partie d'�chec avec quelqu'un!");
                        } else
                            cnx.envoyer("Le salon priv� avec " + aliasInvite + " n'existe pas!");
                    break;
                case "MOVE" :
                    aliasExpediteur = cnx.getAlias();
                    deplacement = evenement.getArgument();
                    indexSalonPrive = serveur.rechercheSalonPrive(aliasExpediteur);
                    if (indexSalonPrive != -1 && serveur.salonsPrives.get(indexSalonPrive).getPartieEchecs() != null) {
                        partieEchecs = serveur.salonsPrives.get(indexSalonPrive).getPartieEchecs();
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
                                if (Character.isLetter(c)) {
                                } else if (Character.isDigit(c)) {
                                    digits += c;
                                }
                            }

                            // V�rification du format
                            if (chars.length == 4 && digits.length() == 2) {
                                char colonne1 = chars[0];
                                byte ligne1 = Byte.parseByte(digits.substring(0, 1));
                                char colonne2 = chars[2];
                                byte ligne2 = Byte.parseByte(digits.substring(1, 2));

                                String deplacementRoqueRoi = "h" + ligne1 + "f" + ligne1;
                                String deplacementRoqueDame = "a" + ligne1 + "d" + ligne1;

                                if (partieEchecs.deplace(new Position(colonne1, ligne1), new Position(colonne2, ligne2))) {
                                    boolean roqueRoiFait = partieEchecs.getRoqueRoiVientEtreFait();
                                    boolean roqueDameFait = partieEchecs.getRoqueDameVientEtreFait();

                                    if (!partieEchecs.estEnEchecEtMat(partieEchecs.getTour())) {
                                        roqueRoiFait = partieEchecs.getRoqueRoiVientEtreFait();
                                        roqueDameFait = partieEchecs.getRoqueDameVientEtreFait();
                                        boolean estEnEchec = partieEchecs.estEnEchec() == couleurJoueurEnnemi;

                                        if ((roqueRoiFait || roqueDameFait) && !estEnEchec) {
                                            String deplacementRoque = roqueRoiFait ? deplacementRoqueRoi : deplacementRoqueDame;
                                            cnx.envoyer("MOVE " + deplacement + deplacementRoque);
                                            serveur.envoyerMessagePrive(aliasJoueurEnnemi).envoyer("MOVE " + deplacement + deplacementRoque);
                                            partieEchecs.setRoqueRoiVientEtreFait(false);
                                            partieEchecs.setRoqueDameVientEtreFait(false);
                                        } else if ((!roqueRoiFait && !roqueDameFait) && !estEnEchec) {
                                            cnx.envoyer("MOVE " + deplacement);
                                            serveur.envoyerMessagePrive(aliasJoueurEnnemi).envoyer("MOVE " + deplacement);
                                        } else if (roqueRoiFait || roqueDameFait) {
                                            String deplacementRoque = roqueRoiFait ? deplacementRoqueRoi : deplacementRoqueDame;
                                            String moveMessage = "ECHEC " + aliasJoueurEnnemi + "/" + deplacement + deplacementRoque;
                                            cnx.envoyer(moveMessage);
                                            serveur.envoyerMessagePrive(aliasJoueurEnnemi).envoyer(moveMessage);
                                            partieEchecs.setRoqueRoiVientEtreFait(false);
                                            partieEchecs.setRoqueDameVientEtreFait(false);
                                        } else {
                                            String moveMessage = "ECHEC " + aliasJoueurEnnemi + "/" + deplacement;
                                            cnx.envoyer(moveMessage);
                                            serveur.envoyerMessagePrive(aliasJoueurEnnemi).envoyer(moveMessage);
                                        }
                                    } else {
                                        if (roqueRoiFait || roqueDameFait) {
                                            String deplacementRoque = roqueRoiFait ? deplacementRoqueRoi : deplacementRoqueDame;
                                            String moveMessage = "MAT " + aliasExpediteur + "/" + deplacement + deplacementRoque;
                                            cnx.envoyer(moveMessage);
                                            serveur.envoyerMessagePrive(aliasJoueurEnnemi).envoyer(moveMessage);
                                            partieEchecs.setRoqueRoiVientEtreFait(false);
                                            partieEchecs.setRoqueDameVientEtreFait(false);
                                        } else {
                                            cnx.envoyer("MAT " + aliasExpediteur + "/" + deplacement);
                                            serveur.envoyerMessagePrive(aliasJoueurEnnemi).envoyer("MAT " + aliasExpediteur + "/" + deplacement);
                                            serveur.salonsPrives.get(indexSalonPrive).setPartieEchecs(null);
                                        }
                                    }
                                } else
                                    cnx.envoyer("INVALID");
                            } else
                                cnx.envoyer("INVALID");
                        }
                    } else
                        cnx.envoyer("Vous n'�tes pas dans une partie d'�chec!");
                    break;
                case "ABANDON" : //Abandonne une partie d��checs.
                    aliasExpediteur = cnx.getAlias();
                    indexSalonPrive = serveur.rechercheSalonPrive(aliasExpediteur);
                    if (indexSalonPrive != -1 && serveur.salonsPrives.get(indexSalonPrive).getPartieEchecs() != null) {
                        partieEchecs = serveur.salonsPrives.get(indexSalonPrive).getPartieEchecs();
                        String aliasEnnemi = aliasExpediteur.equals(partieEchecs.getAliasJoueur1()) ?
                                partieEchecs.getAliasJoueur2() : partieEchecs.getAliasJoueur1();

                        cnx.envoyer("ABANDON " + aliasEnnemi);
                        serveur.envoyerMessagePrive(aliasEnnemi).envoyer("ABANDON " + aliasEnnemi);
                        serveur.salonsPrives.get(indexSalonPrive).setPartieEchecs(null);
                    } else
                        cnx.envoyer("Vous n'�tes pas dans une partie d'�chec!");
                    break;
                default: //Renvoyer le texte recu convertit en majuscules :
                    msg = (evenement.getType() + " " + evenement.getArgument()).toUpperCase();
                    cnx.envoyer(msg);
            }
        }
    }
}
