# Application de Gestion de Tâches Android

Cette application Android permet de gérer des listes de tâches avec une fonctionnalité de mode hors ligne.

## Prérequis

- Android Studio Hedgehog | 2023.1.1 ou version ultérieure
- JDK 17 ou version ultérieure
- Un appareil Android (émulateur ou physique) avec Android 6.0 (API 23) ou supérieur

## Installation

1. Clonez le dépôt :
```bash
git clone [URL_DU_REPO]
```

2. Ouvrez le projet dans Android Studio :
   - Lancez Android Studio
   - Sélectionnez "Open an Existing Project"
   - Naviguez jusqu'au dossier du projet et sélectionnez-le

3. Attendez que Gradle synchronise le projet (cela peut prendre quelques minutes lors de la première ouverture)

4. Connectez un appareil Android ou lancez un émulateur

## Lancement de l'Application

1. Dans Android Studio, cliquez sur le bouton "Run" (triangle vert) dans la barre d'outils
2. Sélectionnez votre appareil/émulateur
3. L'application devrait se lancer automatiquement

## Fonctionnalités Principales

- **Authentification** : Connexion avec un identifiant et un mot de passe
- **Gestion des Listes** : 
  - Création de nouvelles listes
  - Affichage des listes existantes
  - Suppression de listes
- **Gestion des Tâches** :
  - Ajout de nouvelles tâches
  - Marquage des tâches comme terminées
  - Suppression de tâches
- **Mode Hors Ligne** :
  - Création et modification des listes et tâches sans connexion internet
  - Synchronisation automatique lors du retour en ligne

## Architecture Technique

- **Base de Données** : Room Database pour le stockage local
- **API** : Interface REST pour la synchronisation avec le serveur
- **Pattern** : Repository pour la gestion des données
- **Coroutines** : Pour les opérations asynchrones
- **Flow** : Pour l'observation des changements de données

## Structure du Projet

```
app/
├── api/           # Interface API et modèles de réponse
├── database/      # Entités et DAOs Room
├── repository/    # Logique de gestion des données
└── ui/           # Activités et composants d'interface
```

## Dépannage

Si vous rencontrez des problèmes :

1. **Erreur de compilation** :
   - Vérifiez que vous avez la bonne version de JDK
   - Faites "File > Invalidate Caches / Restart"

2. **Problèmes de connexion** :
   - Vérifiez votre connexion internet
   - Assurez-vous que l'API est accessible

3. **Problèmes de base de données** :
   - Désinstallez l'application et réinstallez-la
   - Vérifiez les logs dans Android Studio

## Contribution

Pour contribuer au projet :

1. Forkez le projet
2. Créez une branche pour votre fonctionnalité
3. Committez vos changements
4. Poussez vers votre fork
5. Créez une Pull Request

## Licence

Ce projet est sous licence [à spécifier]

## Contact

Pour toute question, contactez [vos coordonnées] 