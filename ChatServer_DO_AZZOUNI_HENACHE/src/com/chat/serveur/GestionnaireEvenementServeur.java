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
 * Cette classe représente un gestionnaire d'événement d'un serveur. Lorsqu'un serveur reçoit un texte d'un client,
 * il crée un événement à partir du texte reçu et alerte ce gestionnaire qui réagit en gérant l'événement.
 *
 * @author Abdelmoumène Toudeft (Abdelmoumene.Toudeft@etsmtl.ca)
 * @version 1.0
 * @since 2023-09-01
 */
public class GestionnaireEvenementServeur implements GestionnaireEvenement {
    private Serveur serveur;

    /**
     * Construit un gestionnaire d'événements pour un serveur.
     *
     * @param serveur Serveur Le serveur pour lequel ce gestionnaire gère des événements
     */
    public GestionnaireEvenementServeur(Serveur serveur) {
        this.serveur = serveur;
    }

    /**
     * Méthode de gestion d'événements. Cette méthode contiendra le code qui gère les réponses obtenues d'un client.
     *
     * @param evenement L'événement à gérer.
     */
    @Override
    public void traiter(Evenement evenement) {
        Object source = evenement.getSource();
        Connexion cnx;
        String msg, typeEvenement, aliasExpediteur, aliasInvite, listInvitations;
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
                case "EXIT": //Ferme la connexion avec le client qui a envoyé "EXIT":
                    cnx.envoyer("END");
                    serveur.enlever(cnx);
                    cnx.close();
                    break;
                case "LIST": //Envoie la liste des alias des personnes connectées :
                    cnx.envoyer("LIST " + serveur.list());
                    break;
                case "MSG": //Envoie un message à tous les utilisateurs connectés sauf l'expéditeur :
                    aliasExpediteur = cnx.getAlias();
                    msg = evenement.getArgument();
                    serveur.envoyerATousSauf(msg, aliasExpediteur);
                    break;
                case "HIST": //Affiche l'historique des messages :
                    cnx.envoyer("HIST " + serveur.historique());
                    break;
                case "JOIN": //Invite un utilisateur à chatter en privé ou accepte l’invitation qui lui a été
                    //préalablement envoyée par un utilisateur :
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
                            cnx.envoyer("Succès de l'invitation!");
                            serveur.envoyerMessagePrive(aliasInvite).envoyer("JOIN " + aliasExpediteur);
                        }
                    }

                    if (indexSalonPrive != -1)
                        cnx.envoyer("Le salon privé avec " + aliasInvite + " existe déja!");

                    if (!utilisateurExiste)
                        cnx.envoyer("L'utilisateur " + aliasInvite + " n'existe pas!");

                    if (invitationSoi)
                        cnx.envoyer("Vous ne pouvez pas vous inviter vous-même!");
                    break;
                case "DECLINE": //Refuse une invitation à chatter en privé d'un utilisateur ou annule une invitation
                    //qu'il a préalablement envoyée à un utilisateur :
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
                                cnx.envoyer("Aucune invitation à refuser ou à annuler en lien avec l'utilisateur "
                                        + aliasInvite + "!");
                            }
                        } else {
                            invitation = serveur.verifierExistenceInvitation(aliasExpediteur, aliasInvite, true);

                            if (invitation != null) {
                                serveur.traiterDeclineInvitation(cnx, aliasExpediteur, aliasInvite, invitation);
                            } else {
                                cnx.envoyer("Aucune invitation à refuser ou à annuler en lien avec l'utilisateur "
                                        + aliasInvite + "!");
                            }
                        }
                    }

                    if (!utilisateurExiste)
                        cnx.envoyer("L'utilisateur " + aliasInvite + " n'existe pas!");

                    if (invitationSoi)
                        cnx.envoyer("Vous ne pouvez pas vous refuser ou annuler une invitation à vous-même!");
                    break;
                case "INV": //Obtenir la liste de tous les alias des personnes qui lui ont envoyé des invitations :
                    aliasExpediteur = cnx.getAlias();
                    listInvitations = serveur.listInvitations(aliasExpediteur);
                    cnx.envoyer("INV " + listInvitations);
                    break;
                case "PRV": //Envoyer un message à un utilisateur dans un salon privé :
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
                        cnx.envoyer("Le salon privé avec " + aliasInvite + " n'existe pas!");
                    break;
                case "QUIT": //Quitter le salon privé avec un utilisateur :
                    aliasExpediteur = cnx.getAlias();
                    aliasInvite = evenement.getArgument();

                    indexSalonPrive = serveur.verifierExistenceSalonPrive(aliasExpediteur, aliasInvite);

                    if (indexSalonPrive != -1) {
                        serveur.salonsPrives.remove(new SalonPrive(aliasExpediteur, aliasInvite));
                        cnx.envoyer("Vous avez quitté le salon privé avec succès!");
                        serveur.envoyerMessagePrive(aliasInvite).envoyer("QUIT " + aliasExpediteur);
                    } else
                        cnx.envoyer("Le salon privé avec " + aliasInvite + " n'existe pas!");
                    break;
                case "CHESS": //Invite ou accepte une invitation pour une partie de jeu d’échecs
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
                                cnx.envoyer("Succès de l'invitation!");
                                serveur.envoyerMessagePrive(aliasInvite).envoyer("CHESS " + aliasExpediteur);
                            }
                        } else
                            cnx.envoyer("Vous êtes déjà en partie d'échec avec quelqu'un!");
                    } else
                        cnx.envoyer("Le salon privé avec " + aliasInvite + " n'existe pas!");
                    break;
                case "MOVE" : //Effectue un déplacement de pièce dans une partie de jeu d’échecs
                    serveur.traiterMouvement(evenement, cnx);
                    break;
                case "ABANDON" : //Abandonne une partie d’échecs.
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
                        cnx.envoyer("Vous n'êtes pas dans une partie d'échec!");
                    break;
                default: //Renvoyer le texte recu convertit en majuscules :
                    msg = (evenement.getType() + " " + evenement.getArgument()).toUpperCase();
                    cnx.envoyer(msg);
            }
        }
    }
}
