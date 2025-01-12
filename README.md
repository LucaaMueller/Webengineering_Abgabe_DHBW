Dieses Projekt wurde im Rahmen der 5. Theoriephase in der Vorlesung Webengineering 2 an der DHBW Karlsruhe erstellt. 

Wichtige Links für das Projekt:
https://gitlab.com/biletado/quickstart
https://gitlab.com/biletado/apidocs

Vor dem Starten des Projekts müssen folgende Schritte durchgeführt werden:
1. Podman aktivieren 
2. Kind-Cluster aktivieren
3. localhost:9090 aufrufen -> Rapidocs 
4. Für Datenbankverbindung:

```bash
   kubectl port-forward services/postgres 5432:5432
```

Docker/Podman Image
Das Image meiner Anwendung ist in GitHub Packages verfügbar.

Image Details
Registry: ghcr.io
Repository: lucaamueller/webengeneering_abgabe_dhbw
Tag: v1

Container starten
Laden Sie das Image herunter:

```bash
   podman pull ghcr.io/lucaamueller/webengeneering_abgabe_dhbw:v1

Starten Sie den Container:

```bash
   podman run -d --name webeng_container -p 8080:8080 ghcr.io/lucaamueller/webengeneering_abgabe_dhbw:v1

Stellen Sie sicher, dass der Container läuft:

```bash
   podman ps

Greifen Sie auf die Anwendung zu:
Öffnen Sie einen Browser und navigieren Sie zu: http://localhost:8080

