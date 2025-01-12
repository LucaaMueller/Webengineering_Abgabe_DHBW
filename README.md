# README

## Projektbeschreibung
Dieses Projekt wurde im Rahmen der 5. Theoriephase in der Vorlesung Webengineering 2 an der DHBW Karlsruhe erstellt.

### Wichtige Links:
- [Quickstart-Dokumentation](https://gitlab.com/biletado/quickstart)
- [API-Dokumentation](https://gitlab.com/biletado/apidocs)

---

## Voraussetzungen vor dem Start

1. **Podman aktivieren**
2. **Kind-Cluster starten**
3. Rufen Sie `http://localhost:9090` auf, um Rapidocs zu verwenden.
4. **Datenbankverbindung herstellen:**
   ```bash
   kubectl port-forward services/postgres 5432:5432
   ```

---

## Docker/Podman Image

Das Image meiner Anwendung ist in GitHub Packages verfügbar:

- **Registry:** `ghcr.io`
- **Repository:** `lucaamueller/webengeneering_abgabe_dhbw`
- **Tag:** `v1`

### Schritte zum Starten des Containers

1. **Image herunterladen:**
   ```bash
   podman pull ghcr.io/lucaamueller/webengeneering_abgabe_dhbw:v1
   ```

2. **Container starten:**
   ```bash
   podman run -d --name webeng_container -p 8080:8080 ghcr.io/lucaamueller/webengeneering_abgabe_dhbw:v1
   ```

3. **Container-Status überprüfen:**
   ```bash
   podman ps
   ```

4. **Auf die Anwendung zugreifen:**
   - URL: [http://localhost:8080](http://localhost:8080)

---

## Umgebungsvariablen

Die Anwendung kann durch folgende Variablen konfiguriert werden, um interne Einstellungen zu steuern:

### Allgemeine Variablen:
- `POSTGRES_ASSETS_PORT`
- `POSTGRES_ASSETS_HOST`
- `POSTGRES_ASSETS_DBNAME`
- `POSTGRES_ASSETS_PASSWORD`
- `POSTGRES_ASSETS_USER`
- `KEYCLOAK_REALM`
- `KEYCLOAK_HOST`
- `KEYCLOAK_PORT`
- `KEYCLOAK_AUDIENCE`

### Authentifizierung:
Die Authentifizierung erfolgt über einen Keycloak-Server. Dazu müssen folgende Umgebungsvariablen gesetzt werden:
- `KEYCLOAK_REALM`
- `KEYCLOAK_HOST`
- `KEYCLOAK_PORT`
- `KEYCLOAK_AUDIENCE`

---

## Biletado mit eigener API als Cluster betreiben

Die angepasste `kustomization.yaml` befindet sich im Root-Ordner:

### Beispiel:
```yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

resources:
  - https://gitlab.com/biletado/kustomize.git//overlays/kind

patches:
  - patch: |-
      - op: replace
        path: /spec/template/spec/containers/0/image
        value: ghcr.io/lucaamueller/webengeneering_abgabe_dhbw:latest
      - op: replace
        path: /spec/template/spec/containers/0/ports/0/containerPort
        value: 8080
      - op: replace
        path: /spec/template/spec/containers/0/env
        value:
          - name: POSTGRES_ASSETS_HOST
            value: postgres
          - name: POSTGRES_ASSETS_PORT
            value: "5432"
          - name: POSTGRES_ASSETS_USER
            value: postgres
          - name: POSTGRES_ASSETS_PASSWORD
            value: postgres
          - name: POSTGRES_ASSETS_DBNAME
            value: assets_v3
          - name: KEYCLOAK_REALM
            value: biletado
          - name: KEYCLOAK_HOST
            value: keycloak
          - name: KEYCLOAK_PORT
            value: "8080"
```

### Schritt 1: Hochfahren des originalen Biletado-Clusters
Führen Sie folgende Befehle aus:
```bash
kubectl create namespace biletado
kubectl config set-context --current --namespace biletado
kubectl apply -k https://gitlab.com/biletado/kustomize.git//overlays/kind?ref=main --prune -l app.kubernetes.io/part-of=biletado -n biletado
kubectl rollout status deployment -n biletado -l app.kubernetes.io/part-of=biletado --timeout=600s
kubectl wait pods -n biletado -l app.kubernetes.io/part-of=biletado --for condition=Ready --timeout=120s
```

### Schritt 2: Eigene API integrieren
Wechseln Sie in den Ordner mit der `kustomization.yaml` und führen Sie folgenden Befehl aus:
```bash
kubectl apply -k . --prune -l app.kubernetes.io/part-of=biletado -n biletado
kubectl wait pods -n biletado -l app.kubernetes.io/part-of=biletado --for condition=Ready --timeout=120s
```



