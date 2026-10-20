# Atelier 5 : Spring MVC et SpringDoc — API REST, Postman et documentation Swagger

## Section A. Endpoints

| Contrôleur | Verbe & Route | Service appelé | Code succès | Codes erreur |
|---|---|---|---|---|
| `AgenceController` | `GET /api/agences` | `IAgenceService.findAll()` | 200 OK | — |
| `AgenceController` | `GET /api/agences/{id}` | `IAgenceService.findById(id)` | 200 OK | 404 Not Found |
| `AgenceController` | `POST /api/agences` | `IAgenceService.create(agence)` | 201 Created | 400 Bad Request |
| `AgenceController` | `PUT /api/agences/{id}` | `IAgenceService.update(id, agence)` | 200 OK | 400 Bad Request, 404 Not Found |
| `AgenceController` | `DELETE /api/agences/{id}` | `IAgenceService.deleteById(id)` | 204 No Content | 404 Not Found |
| `ClientController` | `GET /api/clients` | `IClientService.findAll()` | 200 OK | — |
| `ClientController` | `GET /api/clients/{id}` | `IClientService.findById(id)` | 200 OK | 404 Not Found |
| `ClientController` | `POST /api/clients` | `IClientService.create(client)` | 201 Created | 400 Bad Request |
| `ClientController` | `PUT /api/clients/{id}` | `IClientService.update(id, client)` | 200 OK | 400 Bad Request, 404 Not Found |
| `ClientController` | `DELETE /api/clients/{id}` | `IClientService.deleteById(id)` | 204 No Content | 404 Not Found |
| `VehiculeController` | `GET /api/vehicules` | `IVehiculeService.findAll()` | 200 OK | — |
| `VehiculeController` | `GET /api/vehicules/{id}` | `IVehiculeService.findById(id)` | 200 OK | 404 Not Found |
| `VehiculeController` | `POST /api/vehicules` | `IVehiculeService.create(vehicule)` | 201 Created | 400 Bad Request |
| `VehiculeController` | `PUT /api/vehicules/{id}` | `IVehiculeService.update(id, vehicule)` | 200 OK | 400 Bad Request, 404 Not Found |
| `VehiculeController` | `DELETE /api/vehicules/{id}` | `IVehiculeService.deleteById(id)` | 204 No Content | 404 Not Found |
| `ContratController` | `GET /api/contrats` | `IContratService.findAll()` | 200 OK | — |
| `ContratController` | `GET /api/contrats/{id}` | `IContratService.findById(id)` | 200 OK | 404 Not Found |
| `ContratController` | `POST /api/contrats` | `IContratService.create(contrat)` | 201 Created | 400 Bad Request |
| `ContratController` | `PUT /api/contrats/{id}` | `IContratService.update(id, contrat)` | 200 OK | 400 Bad Request, 404 Not Found |
| `ContratController` | `DELETE /api/contrats/{id}` | `IContratService.deleteById(id)` | 204 No Content | 404 Not Found |
| `PaiementController` | `GET /api/paiements` | `IPaiementService.findAll()` | 200 OK | — |
| `PaiementController` | `GET /api/paiements/{id}` | `IPaiementService.findById(id)` | 200 OK | 404 Not Found |

---

## Section B. Annotations Jackson

| Association | Annotation retenue | Justification |
|---|---|---|
| `Contrat ↔ Paiement` | `@JsonManagedReference("contrat-paiements")` sur `Contrat.paiements`<br>`@JsonBackReference("contrat-paiements")` sur `Paiement.contrat` | Vraie composition avec cascade `ALL` : les paiements font partie intégrante du contrat lors de la sérialisation, tout en évitant la boucle de sérialisation infinie (`StackOverflowError`). |
| `Agence ↔ Vehicule` | `@JsonIgnore` sur `Agence.vehicules` | Les véhicules sont créés indépendamment en désignant leur agence parente par un identifiant. Évite d'embarquer toute la flotte dans le JSON d'une agence. |
| `Agence ↔ Employe` | `@JsonIgnore` sur `Agence.employes` | L'employé référence son agence, on évite le chargement et la sérialisation imbriquée cyclique des employés dans l'agence. |
| `Client ↔ Reservation` | `@JsonIgnore` sur `Client.reservations` | Évite la boucle lors de la consultation d'un client et permet au client d'être référencé par les réservations. |
| `Vehicule ↔ Maintenance` | `@JsonIgnore` sur `Vehicule.maintenances` | Évite la boucle cyclique avec les fiches de maintenance associées au véhicule. |

### Différence entre `@JsonBackReference` et `@JsonIgnore`
* `@JsonBackReference` fait partie d'un couple avec `@JsonManagedReference` : elle omet le parent lors de la sérialisation pour casser le cycle, mais rétablit automatiquement le lien bidirectionnel parent-enfant lors de la désérialisation JSON (utile en cas de cascade).
* `@JsonIgnore` ignore purement et simplement le champ annoté à la sérialisation et à la désérialisation sans aucune gestion de lien bidirectionnel.

---

## Section C. Postman et Swagger

| Outil | Rôle & Apport | Exemple concret rencontré dans l'Atelier |
|---|---|---|
| **Postman** | Outil client d'exécution et de test dynamique des requêtes HTTP (avec variables d'environnement, assertions de statut et scripts automatisés). | Écriture des requêtes de test automatisées (ex. `pm.test("Statut 201", () => pm.response.to.have.status(201));` et vérification de la cascade `POST /api/contrats` créant des paiements). |
| **Swagger / OpenAPI** | Documentation vivante synchronisée avec le code, permettant d'explorer le contrat d'interface et d'essayer directement les endpoints via interface web. | Visualisation interactive de l'arbre d'API sous `http://localhost:8080/api/swagger-ui.html` avec les schémas des entités, résumés d'opérations et codes d'erreur documentés. |

---

## Section D. Anomalies SonarQube for IDE

| Règle SonarQube | Intitulé & Explication | Correction apportée |
|---|---|---|
| **S6813** | *Field dependency injection should not be used* : L'injection par champ (`@Autowired`) masque les dépendances et empêche les tests unitaires isolés. | Injection par constructeur via `@RequiredArgsConstructor` sur tous les contrôleurs avec attributs `private final`. |
| **S112** | *Generic exceptions should never be thrown* : Utiliser `RuntimeException` ne permet pas de caractériser les erreurs HTTP. | Utilisation de `ResourceNotFoundException` interceptée par le `@RestControllerAdvice` pour retourner un code 404 propre. |

---

## Section E. Questions de compréhension

1. **Quel composant reçoit toutes les requêtes, et comment retrouve-t-il la méthode du contrôleur à appeler ?**
   C'est la servlet frontale **`DispatcherServlet`** de Spring MVC. Elle interroge le composant **`HandlerMapping`** qui inspecte les métadonnées (`@RequestMapping`, `@GetMapping`, etc.) enregistrées au démarrage pour router l'URL et le verbe HTTP vers la méthode du bon contrôleur.

2. **Pourquoi `ContratController` dépend-il de `IContratService` et non de `ContratRepository` ni de `ContratServiceImpl` ?**
   * Par rapport à `ContratRepository` : pour respecter la séparation des couches (un contrôleur ne doit pas accéder directement aux données sans passer par la logique métier).
   * Par rapport à `ContratServiceImpl` : pour respecter le principe d'inversion des dépendances (DIP) en dépendant d'une abstraction (l'interface), facilitant le couplage faible et l'interchangeabilité (ex. pour les mocks de test).

3. **Pourquoi un POST renvoie-t-il 201 et un DELETE 204 ? Pourquoi une ressource absente donne-t-elle 404 et non 500 ?**
   * **201 Created** indique qu'une nouvelle ressource a été créée avec succès sur le serveur suite à la requête.
   * **204 No Content** indique que l'action a réussi et qu'il n'y a aucun contenu à renvoyer dans le corps de réponse.
   * Une ressource absente est une situation normale de consultation client (erreur côté client : **404 Not Found**). Un code **500 Internal Server Error** signale un bug inattendu ou un crash du serveur, ce qui n'est pas le cas ici.

4. **Pourquoi `PaiementController` n'expose-t-il que des GET ? Quelle réponse reçoit un POST `/api/paiements` ?**
   * Parce que les paiements dépendent strictement du contrat de location auquel ils sont rattachés : leur création et modification se font exclusivement à travers le Contrat (par cascade).
   * Un `POST /api/paiements` reçoit un code **405 Method Not Allowed** car aucune méthode POST n'est exposée sur cette route.

5. **Quels risques présente l'exposition directe des entités JPA ? Pourquoi `Contrat.paiements` utilise-t-il `@JsonManagedReference` alors que `Agence.vehicules` utilise `@JsonIgnore` ?**
   * Risques de l'exposition directe : fuite de champs internes sensibles (mots de passe, identifiants techniques), cycles de sérialisation infinis, couplage fort entre le modèle relationnel de la base et le contrat de l'API REST.
   * `Contrat.paiements` utilise `@JsonManagedReference` car il s'agit d'une composition stricte : lors de la création d'un contrat en JSON, on veut pouvoir inclure ses paiements. En revanche, `Agence.vehicules` utilise `@JsonIgnore` car les véhicules ont leur propre cycle de vie et sont créés indépendamment en ciblant l'identifiant de leur agence.
