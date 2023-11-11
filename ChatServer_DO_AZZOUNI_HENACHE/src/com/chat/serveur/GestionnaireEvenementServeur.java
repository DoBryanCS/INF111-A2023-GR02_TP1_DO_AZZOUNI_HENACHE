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
                case "EXIT": //Ferme la connexion avec le client qui a envoyé "EXIT":
                    cnx.envoyer("END");
                    serveur.enlever(cnx);
                    cnx.close();
                    break;
                case "LIST": //Envoie la liste des alias des personnes connectées :
                    cnx.envoyer("LIST " + serveur.list());
                    break;

                //Ajoutez ici d’autres case pour gérer d’autres commandes.

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
                                    cnx.envoyer("Succès de l'invitation!");
                                    serveur.envoyerMessagePrive(aliasInvite).envoyer("CHESS " + aliasExpediteur);
                                }
                            } else
                                cnx.envoyer("Vous êtes déjà en partie d'échec avec quelqu'un!");
                        } else
                            cnx.envoyer("Le salon privé avec " + aliasInvite + " n'existe pas!");

                    break;

                case "MOVE" :
                    aliasExpediteur = cnx.getAlias();
                    deplacement = evenement.getArgument();
                    indexSalonPrive = serveur.rechercheSalonPrive(aliasExpediteur);
                    partieEchecs = serveur.salonsPrives.get(indexSalonPrive).getPartieEchecs();
                    char couleurJoueurActuel = aliasExpediteur.equals(partieEchecs.getAliasJoueur1()) ?
                            partieEchecs.getCouleurJoueur1() : partieEchecs.getCouleurJoueur2();
                    String aliasJoueurEnnemi = aliasExpediteur.equals(partieEchecs.getAliasJoueur1()) ?
                            partieEchecs.getAliasJoueur2() : partieEchecs.getAliasJoueur1();
                    char couleurJoueurEnnemi = couleurJoueurActuel == 'b' ? 'n' : 'b';

                    if (partieEchecs.getTour() != couleurJoueurActuel) {
                        cnx.envoyer("Ce n'est pas votre tour de jouer!");
                    } else {
                        // Définition du motif de la commande
                        String motif = "([a-zA-Z])(\\d+)[-\\s]?([a-zA-Z])(\\d+)";

                        // Création d'un objet Pattern
                        Pattern pattern = Pattern.compile(motif);

                        // Création d'un objet Matcher avec la commande
                        Matcher matcher = pattern.matcher(deplacement);

                        // Vérification du format
                        if (matcher.matches()) {
                            char colonne1 = matcher.group(1).charAt(0);
                            byte ligne1 = Byte.parseByte(matcher.group(2));
                            char colonne2 = matcher.group(3).charAt(0);
                            byte ligne2 = Byte.parseByte(matcher.group(4));

                            if(partieEchecs.deplace(new Position(colonne1, ligne1), new Position(colonne2, ligne2))) {
                                if (!partieEchecs.estEnEchecEtMat(partieEchecs.getTour())) {
                                    cnx.envoyer("MOVE " + deplacement);
                                    serveur.envoyerMessagePrive(aliasJoueurEnnemi).envoyer("MOVE " + deplacement);
                                    if (partieEchecs.estEnEchec() == couleurJoueurEnnemi) {
                                        cnx.envoyer("ECHEC " + aliasJoueurEnnemi);
                                        serveur.envoyerMessagePrive(aliasJoueurEnnemi).envoyer("ECHEC " + aliasJoueurEnnemi);
                                    }
                                } else {
                                    cnx.envoyer("MAT " + aliasExpediteur);
                                    serveur.envoyerMessagePrive(aliasJoueurEnnemi).envoyer("MAT " + aliasExpediteur);
                                }
                            } else {
                                cnx.envoyer("INVALID");
                            }
                        } else
                            cnx.envoyer("INVALID");
                    }

                    break;

                case "ABANDON" : //Abandonne une partie d’échecs.
                    aliasExpediteur = cnx.getAlias();
                    indexSalonPrive = serveur.rechercheSalonPrive(aliasExpediteur);
                    partieEchecs = serveur.salonsPrives.get(indexSalonPrive).getPartieEchecs();
                    String aliasEnnemi = aliasExpediteur.equals(partieEchecs.getAliasJoueur1()) ?
                            partieEchecs.getAliasJoueur2() : partieEchecs.getAliasJoueur1();

                    serveur.salonsPrives.get(indexSalonPrive).setPartieEchecs(null);
                    cnx.envoyer("ABANDON " + aliasEnnemi);
                    serveur.envoyerMessagePrive(aliasEnnemi).envoyer("ABANDON " + aliasEnnemi);
                    break;

                default: //Renvoyer le texte recu convertit en majuscules :
                    msg = (evenement.getType() + " " + evenement.getArgument()).toUpperCase();
                    cnx.envoyer(msg);
            }
        }
    }
}