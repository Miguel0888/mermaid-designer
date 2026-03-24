package de.bund.zrb.ui;

import de.bund.zrb.domain.DiagramType;

/**
 * Provides starter templates for each supported diagram type.
 */
public final class DiagramTemplates {

    private DiagramTemplates() {}

    public static String getTemplate(DiagramType type) {
        return switch (type) {
            case FLOWCHART -> FLOWCHART_TEMPLATE;
            case GRAPH -> GRAPH_TEMPLATE;
            case SEQUENCE -> SEQUENCE_TEMPLATE;
            case CLASS -> CLASS_TEMPLATE;
            case STATE -> STATE_TEMPLATE;
            case ER -> ER_TEMPLATE;
            case PIE -> PIE_TEMPLATE;
            case GANTT -> GANTT_TEMPLATE;
            case JOURNEY -> JOURNEY_TEMPLATE;
            case MINDMAP -> MINDMAP_TEMPLATE;
            case GIT_GRAPH -> GIT_GRAPH_TEMPLATE;
            case C4_CONTEXT -> C4_TEMPLATE;
            case REQUIREMENT -> REQUIREMENT_TEMPLATE;
            default -> DEFAULT_TEMPLATE;
        };
    }

    public static final String DEFAULT_TEMPLATE = """
            flowchart TD
                A[Start] --> B{Entscheidung}
                B -->|Ja| C[Aktion 1]
                B -->|Nein| D[Aktion 2]
                C --> E[Ende]
                D --> E
            """;

    public static final String FLOWCHART_TEMPLATE = """
            flowchart TD
                A[Start] --> B{Entscheidung}
                B -->|Ja| C[Ergebnis A]
                B -->|Nein| D[Ergebnis B]
                C --> E[Ende]
                D --> E
            """;

    public static final String GRAPH_TEMPLATE = """
            graph LR
                A[Modul A] --> B[Modul B]
                A --> C[Modul C]
                B --> D[Modul D]
                C --> D
            """;

    public static final String SEQUENCE_TEMPLATE = """
            sequenceDiagram
                participant U as Benutzer
                participant S as Server
                participant DB as Datenbank
                U->>S: Anfrage senden
                S->>DB: Daten abfragen
                DB-->>S: Ergebnis
                S-->>U: Antwort
            """;

    public static final String CLASS_TEMPLATE = """
            classDiagram
                class Tier {
                    +String name
                    +int alter
                    +geraeuschMachen() String
                }
                class Hund {
                    +String rasse
                    +geraeuschMachen() String
                }
                class Katze {
                    +boolean istStubentiger
                    +geraeuschMachen() String
                }
                Tier <|-- Hund
                Tier <|-- Katze
            """;

    public static final String STATE_TEMPLATE = """
            stateDiagram-v2
                [*] --> Inaktiv
                Inaktiv --> Aktiv : aktivieren
                Aktiv --> Pausiert : pausieren
                Pausiert --> Aktiv : fortsetzen
                Aktiv --> Beendet : stoppen
                Pausiert --> Beendet : abbrechen
                Beendet --> [*]
            """;

    public static final String ER_TEMPLATE = """
            erDiagram
                KUNDE ||--o{ BESTELLUNG : "gibt auf"
                BESTELLUNG ||--|{ POSITION : "enthaelt"
                POSITION }o--|| PRODUKT : "bezieht sich auf"
                KUNDE {
                    int id PK
                    string name
                    string email
                }
                BESTELLUNG {
                    int id PK
                    date datum
                    float betrag
                }
                PRODUKT {
                    int id PK
                    string bezeichnung
                    float preis
                }
            """;

    public static final String PIE_TEMPLATE = """
            pie title Projektaufwand
                "Entwicklung" : 45
                "Testing" : 25
                "Dokumentation" : 15
                "Meetings" : 15
            """;

    public static final String GANTT_TEMPLATE = """
            gantt
                title Projektplan
                dateFormat YYYY-MM-DD
                section Analyse
                    Anforderungen      :a1, 2026-01-01, 14d
                    Spezifikation      :a2, after a1, 7d
                section Entwicklung
                    Backend            :b1, after a2, 21d
                    Frontend           :b2, after a2, 28d
                section Test
                    Integration        :t1, after b1, 14d
                    Abnahme            :t2, after t1, 7d
            """;

    public static final String JOURNEY_TEMPLATE = """
            journey
                title Benutzer-Erfahrung
                section Anmeldung
                    Website oeffnen: 5: Benutzer
                    Login-Seite: 3: Benutzer
                    Zugangsdaten eingeben: 2: Benutzer
                section Dashboard
                    Uebersicht laden: 4: System
                    Daten anzeigen: 5: System
            """;

    public static final String MINDMAP_TEMPLATE = """
            mindmap
                root((Projekt))
                    Planung
                        Anforderungen
                        Zeitplan
                        Ressourcen
                    Entwicklung
                        Backend
                        Frontend
                        Datenbank
                    Testing
                        Unit Tests
                        Integration
                        Abnahme
                    Betrieb
                        Deployment
                        Monitoring
            """;

    public static final String GIT_GRAPH_TEMPLATE = """
            gitGraph
                commit id: "Initial"
                branch develop
                commit id: "Feature A"
                commit id: "Feature B"
                checkout main
                merge develop id: "Release 1.0"
                commit id: "Hotfix"
            """;

    public static final String C4_TEMPLATE = """
            C4Context
                title System Kontext Diagramm
                Person(user, "Benutzer", "Ein Benutzer des Systems")
                System(system, "Hauptsystem", "Das zentrale System")
                System_Ext(email, "E-Mail System", "Externer E-Mail Service")
                Rel(user, system, "Nutzt")
                Rel(system, email, "Sendet E-Mails")
            """;

    public static final String REQUIREMENT_TEMPLATE = """
            requirementDiagram
                requirement Anforderung1 {
                    id: REQ-001
                    text: Das System muss Benutzer authentifizieren
                    risk: high
                    verifymethod: test
                }
                functionalRequirement Anforderung2 {
                    id: REQ-002
                    text: Login mit Benutzername und Passwort
                    risk: medium
                    verifymethod: demonstration
                }
                Anforderung1 - derives -> Anforderung2
            """;
}
