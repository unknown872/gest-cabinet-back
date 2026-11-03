# Gest Cabinet Backend

## Prérequis

#### Avant de commencer, vous devez avoir installé :

Java JDK 25

Maven

Git

PostgreSQL (pgAdmin)

#### Vérifier les installations :

java -version

mvn -version

git --version

psql --version

## Installation
### Cloner le projet
git clone https://github.com/unknown872/gest-cabinet-back.git

cd gest-cabinet-back

### Installer les dépendances

Les dépendances sont définies dans le fichier pom.xml.

Sous Windows :

```powershell
.\mvnw clean install
```

Sous Linux ou macOS :

```bash
./mvnw clean install
```

## Exécution

Pour lancer le projet :

Sous Windows :

```powershell
.\mvnw spring-boot:run
```

Sous Linux ou macOS :

```bash
./mvnw spring-boot:run
```


## Configuration de PostgreSQL

### Prérequis

Chaque membre doit avoir installé PostgreSQL et disposer de ses propres identifiants de connexion.

### 1. Créer la base de données

Dans pgAdmin :

1. Développer `Servers`.
2. Faire un clic droit sur `Databases`.
3. Sélectionner `Create` → `Database`.
4. Nommer la base `gest_cabinet_db`.
5. Cliquer sur `Save`.

Ou exécuter la commande SQL suivante :

```sql
CREATE DATABASE gest_cabinet_db;
```

### 2. Configurer la connexion

Dans `src/main/resources/application.properties`, ajouter ou adapter les propriétés suivantes :

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/gest_cabinet_db
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

- `DB_USERNAME` : utilisateur PostgreSQL, `postgres` par défaut.
- `DB_PASSWORD` : mot de passe de l'utilisateur PostgreSQL.
- `5432` : port PostgreSQL par défaut.

Chaque membre doit configurer ses variables d'environnement `DB_USERNAME` et `DB_PASSWORD` sur son ordinateur.


Remplacer les valeurs par ses propres identifiants. Ne jamais publier de mot de passe réel dans Git.

### 3. Créer les tables

Après la configuration, démarrer l'application :

Sous Windows :

```powershell
.\mvnw spring-boot:run
```

Sous Linux ou macOS :

```bash
./mvnw spring-boot:run
```

Hibernate crée les tables manquantes à partir des entités JPA grâce à :

```properties
spring.jpa.hibernate.ddl-auto=update
```

Dans pgAdmin, actualiser `Schemas` → `public` → `Tables` pour vérifier leur création.

### Remarques

- Le serveur PostgreSQL doit être démarré.
- La base `gest_cabinet_db` doit exister avant le lancement.
- Chaque membre utilise sa propre base locale.
- Les données présentes sur un ordinateur ne sont pas automatiquement transférées aux autres.
- `ddl-auto=update` facilite la création des tables, mais ne remplace pas un système de migrations versionnées.


## Configuration initiale de la sécurité et gestion des utilisateurs

### 1. Création du compte administrateur (IMPORTANT)

Avant de pouvoir utiliser les fonctionnalités de gestion des utilisateurs, chaque membre doit créer manuellement un premier compte administrateur directement dans sa base de données PostgreSQL.

Cette étape est obligatoire pour initialiser la partie sécurité et permettre ensuite à l'administrateur de créer les autres types d'utilisateurs : `PATIENT`, `MEDECIN` et `SECRETAIRE`.

Pour cela, exécuter la requête SQL suivante dans pgAdmin, sur la base de données `gest_cabinet_db` :

```sql
WITH new_user AS (
    INSERT INTO utilisateur (
        nom,
        prenom,
        email,
        mot_de_passe,
        telephone,
        actif,
        role,
        genre,
        adresse,
        date_naissance,
        code
    )
    VALUES (
        'DOE',
        'John',
        'doe@admin.com',
        '$2b$10$zZbPMV9MG6doBWZroEpWwOcudsfvHUkBWhv7EWDrCb1X1hJR5Hcm6',
        '770000000',
        true,
        'ADMINISTRATEUR',
        'MASCULIN',
        'Dakar',
        '1990-01-01',
        'USER-000001'
    )
    RETURNING id
)
INSERT INTO administrateur (
    id,
    code_administrateur
)
SELECT
    id,
    'ADM-000001'
FROM new_user;
```

#### Identifiants du compte administrateur

- **Email :** `doe@admin.com`
- **Mot de passe :** `Patient@2026`

> **Important :** Chaque membre doit exécuter cette requête une seule fois sur sa propre base de données locale. Le compte administrateur est indispensable pour accéder aux fonctionnalités de gestion des utilisateurs.

### 2. Gestion des utilisateurs par l'administrateur

Une fois le compte administrateur créé, connectez-vous avec ses identifiants afin de pouvoir ajouter et consulter les utilisateurs de l'application.

Les endpoints disponibles sont les suivants :

| Opération | Méthode HTTP | Endpoint |
|---|---|---|
| Ajouter un utilisateur | `POST` | `http://localhost:8080/api/utilisateurs` |
| Afficher les utilisateurs | `GET` | `http://localhost:8080/api/utilisateurs` |

#### Ajouter un utilisateur

Pour créer un utilisateur, utilisez la requête suivante dans Postman :

**Endpoint :**

```http
POST http://localhost:8080/api/utilisateurs
```

**Body (JSON) :**

```json
{
    "nom": "Doe",
    "prenom": "John",
    "email": "doe.john@test.com",
    "motDePasse": "Patient@2026",
    "genre": "MASCULIN",
    "adresse": "Dakar",
    "dateNaissance": "2024-01-01",
    "telephone": "770989090",
    "actif": true,
    "role": "PATIENT"
}
```

Vous pouvez également créer des utilisateurs de type `MEDECIN` ou `SECRETAIRE`. Il suffit de modifier la valeur du champ `role` selon le profil souhaité.

Exemples :

- `"role": "PATIENT"`
- `"role": "MEDECIN"`
- `"role": "SECRETAIRE"`

#### Afficher la liste des utilisateurs

Pour consulter les utilisateurs enregistrés dans l'application :

**Endpoint :**

```http
GET http://localhost:8080/api/utilisateurs
```

> **Attention :** N'oubliez pas de vous connecter avec le compte administrateur avant d'effectuer ces opérations. Ces endpoints nécessitent une authentification et les autorisations appropriées.

### 3. Inscription directe d'un patient

Il est également possible de créer directement un compte de type `PATIENT`, sans passer par le compte administrateur.

Pour cela, utilisez l'endpoint d'inscription publique suivant :

- **Méthode HTTP :** `POST`
- **Endpoint :** `http://localhost:8080/api/auth/register`

#### Exemple de requête JSON

```json
{
    "nom": "Doe",
    "prenom": "John",
    "email": "doe.john@test.com",
    "motDePasse": "Patient@2026",
    "genre": "MASCULIN",
    "adresse": "Dakar",
    "dateNaissance": "2024-01-01",
    "telephone": "770989090",
    "actif": true,
    "role": "PATIENT"
}
```

Cette route permet aux patients de s'inscrire directement sur la plateforme sans intervention préalable de l'administrateur.

> **Remarque :** Cette inscription directe concerne uniquement les patients. La création des comptes `MEDECIN` et `SECRETAIRE` doit passer par le compte administrateur.
