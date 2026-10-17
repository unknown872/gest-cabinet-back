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
