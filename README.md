# JGrooveBox 🎹🥁

Un séquenceur rythmique (Drum Machine) 16 pas sur 1 mesure, développé en Java. 

Ce projet m'a permis de travailler sur ces concepts : gestion d'événements temps réel, programmation concurrente (Threads), gestion du MIDI & Audio, et conception d'interfaces graphiques en Java Swing.

---

## 🎯 Fonctionnalités Principales

*   **Séquenceur 16 pas :** Grille intuitive permettant de programmer des rythmes sur 5 pistes simultanées (Kick, Snare, Clap, Closed Hi-hat, Open Hi-hat).
*   **Moteur Audio Polyphonique :** Système de "Voice Pooling" (4 voix par piste) empêchant la coupure brutale d'un son lorsqu'il est redéclenché rapidement.
*   **Synchronisation MIDI :** Utilisation de l'horloge interne `javax.sound.midi` pour un timing rythmique précis.
*   **Mixage Temps Réel :** Contrôle du volume indépendant pour chaque piste.
*   **Contrôle du Tempo :** Ajustement en direct du BPM (50 à 150 BPM).
*   **Persistance des Données :** Sauvegarde et importation des patterns rythmiques via un système d'I/O sur fichier texte (`pattern_sauvegarde.txt`).
*   **Interface Réactive :** Retour visuel / tête de lecture (changement de couleur des pas) synchronisé avec le flux audio via `SwingUtilities.invokeLater`.

---

## 🎧 Easter Egg & Customisation des Sons

Le séquenceur lit les fichiers audio `.wav` situés dans le dossier `sounds/`. **Vous pouvez créer votre propre banque de sons** en remplaçant ces fichiers (veillez simplement à conserver les noms exacts : `kick.wav`, `snare.wav`, etc.).

> **🕵️‍♂️ Le Défi :** 
> Actuellement, les samples de batterie ont été temporairement remplacés par des notes de synthétiseur basse. 
> Lancez le projet et cliquez sur le bouton **"Importer dernier pattern sauvegardé"**, puis appuyez sur Play. Le séquenceur jouera un riff de basse iconique de l'histoire de la musique électronique. 
> *À vous de deviner le morceau !.*

---

## ⚙️ Architecture & Algorithme

Le projet est structuré comme ceci :

### 1. Le Moteur Audio (`SoundPlayer.java`)
L'API `javax.sound.sampled` est utilisée pour charger les fichiers en RAM. Pour éviter le phénomène de "Voice Stealing" (un son trop long qui serait coupé par le coup suivant), le système instancie un **pool de 4 lecteurs (`Clip`) par instrument**. L'algorithme opère une rotation sur ce tableau circulaire pour garantir une lecture la plus fluide possible.

### 2. La gestion Rythmique (`Runnable_Sequencer.java`)
 Plutôt que d'utiliser `Thread.sleep()` (imprécis et sujet aux dérives temporelles en Java), le projet délègue le comptage du temps à l'API **MIDI Hardware/Software du système (`javax.sound.midi.Sequencer`)**. 
* L'application génère une boucle MIDI invisible de 16 tics.
* La classe implémente l'interface `Receiver` pour intercepter chaque tic MIDI.
* À chaque interception, elle déclenche la lecture audio des pistes cochées et notifie l'interface graphique.

### 3. L'Interface Graphique (`Fenetre_JGrooveBox.java`)
Conçue avec `GridBagLayout`, elle utilise un système de `LinkedHashMap` liant chaque nom d'instrument à son tableau d'objets `JToggleButton`. Les événements utilisateur (clics, curseurs) mettent à jour l'état de ces composants, qui sont ensuite lus en temps réel par le séquenceur.

---

## 📖 Guide d'Utilisation

1. **Lancement :** Exécutez la classe `Main.java`. L'interface de la JGrooveBox s'ouvrira.
2. **Programmation :** Cliquez sur les cases blanches de la grille pour placer vos notes. Chaque colonne représente une double-croche (1/16ème de temps).
3. **Lecture / Arrêt :** Utilisez les boutons **Play** et **Stop** pour lancer ou arrêter la séquence.
4. **Mixage :** Ajustez le **Tempo** global via le curseur supérieur, et le **Volume** de chaque instrument via les curseurs situés à côté des noms de pistes.
5. **Sauvegarde :** Cliquez sur **Sauvegarder pattern actuel**. La grille sera enregistrée avec un horodatage dans le fichier `data/pattern_sauvegarde.txt`.
6. **Importation :** Cliquez sur **Importer dernier pattern sauvegardé** pour recharger la dernière rythmique ajoutée au fichier de sauvegarde.

---
*Projet développé dans le but d'allier ingénierie sonore et programmation JAVA.
