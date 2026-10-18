# Atelier 4 : Inversion de contrôle, injection de dépendances et couche Service

## Section A. Modes d'injection comparés

| Critère | Injection par constructeur (recommandé) | Injection par attribut (`@Autowired`) |
|---|---|---|
| **Champ `final` possible ?** | Oui (`private final ...`) garantissant l'immuabilité et l'état complet de l'objet. | Non, les champs annotés par `@Autowired` ne peuvent pas être `final`. |
| **Dépendance visible dans la signature ?** | Oui, le constructeur expose explicitement toutes les dépendances requises. | Non, les dépendances sont cachées et masquées aux utilisateurs de la classe. |
| **Utilisation hors conteneur avec `new` ?** | Oui, l'instanciation manuelle (ex. dans des tests unitaires) est directe en passant les mocks/collaborateurs. | Non, l'instanciation par `new` laisse les champs à `null`, risquant des `NullPointerException`. |
| **Signalé par SonarQube for IDE ?** | Non, conforme aux bonnes pratiques Clean Code et standards Spring Boot. | Oui, violation de la règle SonarQube **S6813** (*"Field dependency injection should not be used"*). |

---

## Section B. Services

| Service | Dépendances injectées | Mode d'injection | Justification |
|---|---|---|---|
| `ContratServiceImpl` | `IContratRepository` | Constructeur (`@RequiredArgsConstructor`) | Accès aux contrats pour CRUD et cascade sur paiements. |
| `AgenceServiceImpl` | `IAgenceRepository` | Constructeur (`@RequiredArgsConstructor`) | Persistance et validation des données d'agences. |
| `ClientServiceImpl` | `IClientRepository` | Constructeur (`@RequiredArgsConstructor`) | Gestion du cycle de vie des clients et validation identité/email. |
| `EmployeServiceImpl` | `IEmployeRepository` | Constructeur (`@RequiredArgsConstructor`) | Gestion des employés avec validation des attributs obligatoires (nom, rôle). |
| `EquipementServiceImpl` | `IEquipementRepository` | Constructeur (`@RequiredArgsConstructor`) | Persistance des équipements et validation du libellé obligatoire. |
| `MaintenanceServiceImpl` | `IMaintenanceRepository` | Constructeur (`@RequiredArgsConstructor`) | Suivi de l'entretien et validation de la cohérence des dates début/fin. |
| `PaiementServiceImpl` | `IPaiementRepository` | Constructeur (`@RequiredArgsConstructor`) | Consultation en lecture seule (création/modification déléguées au Contrat). |
| `ReservationServiceImpl` | `IReservationRepository` | Constructeur (`@RequiredArgsConstructor`) | Gestion des réservations avec validation temporelle (date fin >= date début). |
| `VehiculeServiceImpl` | `IVehiculeRepository` | Constructeur (`@RequiredArgsConstructor`) | Gestion de la flotte et validation des valeurs positives (tarif journalier). |

---

## Section C. Messages d'erreur

### Message 1
* **Erreur** : `Parameter 0 of constructor in ContratController required a bean of type 'IContratService' that could not be found.`
* **Causes probables côté `ContratServiceImpl`** :
  1. L'annotation de stéréotype `@Service` ou `@Component` a été oubliée sur la classe `ContratServiceImpl`.
  2. La classe `ContratServiceImpl` est située dans un package en dehors du scan de composants de Spring Boot (hors du package racine `tn.esprit.autoloc` ou ses sous-packages).
* **Correction** : Ajouter `@Service` sur `ContratServiceImpl` et s'assurer qu'elle est bien dans le package `tn.esprit.autoloc.service`.

### Message 2
* **Erreur** : `required a single bean, but 2 were found: emailNotificateur, smsNotificateur`
* **Deux corrections possibles** :
  1. **Option 1 (`@Primary`)** : Placer `@Primary` sur la classe du bean préféré (ex. `@Primary` sur `EmailNotificateur`) pour en faire le choix par défaut.
  2. **Option 2 (`@Qualifier`)** : Placer `@Qualifier("emailNotificateur")` au niveau de l'injection (sur le paramètre du constructeur du consommateur `NotificationController`) pour lever explicitement l'ambiguïté.

### Message 3
* **Erreur** : `The dependencies of some of the beans in the application context form a cycle: clientServiceImpl <--> reservationServiceImpl`
* **Pourquoi l'injection par constructeur rend-elle ce cycle impossible à contourner ?**
  Parce qu'à l'instanciation, pour créer `ClientServiceImpl`, Spring doit d'abord instancier `ReservationServiceImpl`, qui à son tour requiert `ClientServiceImpl`. Aucun constructeur ne peut terminer son exécution, provoquant un blocage d'instanciation instantané (`BeanCurrentlyInCreationException`).
* **Piste de conception pour le supprimer** :
  Revoir l'architecture et extraire la responsabilité commune dans un troisième service médiateur (ex. `ReservationClientFacade` ou un service d'orchestration), ou faire en sorte que l'un des services n'utilise que le Repository correspondant de l'autre entité plutôt que le service complet.

---

## Section D. Anomalies SonarQube for IDE

| Règle SonarQube | Intitulé & Explication | Correction apportée |
|---|---|---|
| **S6813** | *Field dependency injection should not be used* : L'injection par champ `@Autowired` masque les dépendances et empêche l'instanciation hors conteneur. | Remplacement par l'injection par constructeur via Lombok `@RequiredArgsConstructor` avec champs `private final`. |
| **S112** | *Generic exceptions should never be thrown* : Lever des `RuntimeException` génériques ne permet pas d'identifier précisément l'erreur métier ni de lui associer un code HTTP adapté. | Création et utilisation de la classe d'exception métier dédiée `ResourceNotFoundException`. |
| **S1186** | *Methods should not be empty* / *Unused imports* : Imports inutilisés ou classes vides polluent le code. | Nettoyage complet des imports et implémentation rigoureuse de chaque méthode CRUD avec validations métier. |

---

## Section E. Questions de compréhension

1. **Où est le `new` de `ContratServiceImpl` et qui l'exécute ?**
   Il se trouve à l'intérieur du conteneur Spring IoC (`ApplicationContext`). Lors du démarrage (`SpringApplication.run`), Spring scanne les classes annotées `@Service`, instancie leurs dépendances (le proxy Spring Data de `IContratRepository`), puis invoque par réflexion le constructeur de `ContratServiceImpl`.

2. **Pourquoi un contrôleur dépendra-t-il de `IContratService` et non de `ContratServiceImpl` ?**
   Pour respecter le principe d'inversion des dépendances (DIP des principes SOLID) : les modules de haut niveau ne doivent pas dépendre des modules de bas niveau, mais d'abstractions. Cela permet de substituer l'implémentation (par exemple par un mock lors des tests unitaires ou par une autre implémentation métier) sans impacter le contrôleur.

3. **Pourquoi un service singleton doit-il rester sans état ? Donner un exemple de bug.**
   Un service Spring est un singleton partagé par l'ensemble des requêtes HTTP et des threads de l'application. S'il conserve un état mutable dans un attribut d'instance (ex. `private Contrat contratCourant;`), une requête pour le client A peut écraser ou lire les données d'une requête concurrente pour le client B (*race condition* / violation de confidentialité).

4. **Pourquoi `update` charge-t-il l'entité existante avant d'appeler `save` au lieu de lui passer directement l'objet reçu ?**
   Passer l'objet reçu directement écraserait aveuglément tous les champs non renseignés dans la requête (les mettant à `null`), et romprait les collections associées (ex. paiements liés avec `orphanRemoval = true`, qui risqueraient d'être supprimés par inadvertance en base de données). Charger l'existant permet de ne recopier sélectivement que les champs modifiables.

5. **Quelle différence entre `@Component` et `@Bean` ? Entre `@Primary` et `@Qualifier` ? Pourquoi `IPaiementService` n'expose-t-il ni création, ni modification, ni suppression ?**
   * **`@Component` vs `@Bean`** : `@Component` est une annotation de classe détectée automatiquement par le scan de composants. `@Bean` est une annotation de méthode (dans une classe `@Configuration`) utilisée pour créer et configurer un bean manuellement (indispensable pour les bibliothèques tierces non annotées).
   * **`@Primary` vs `@Qualifier`** : `@Primary` définit le candidat par défaut lorsqu'il existe plusieurs beans de même type. `@Qualifier("nom")` permet de spécifier explicitement au point d'injection quel bean précis injecter.
   * **Pourquoi `IPaiementService` est en lecture seule** : Un paiement est intrinsèquement rattaché au cycle de vie comptable d'un contrat de location. Sa création, validation ou annulation doit impérativement être orchestrée par le service du Contrat (`ContratService`) pour garantir la cohérence des montants et des soldes.
