# java
- just used a first available java version
  - [ ] it could be changed

# Kommunikation Client --> Server
- Server können die Ports für die Clients in localhost freigeben

# Kommunikation Server --> Client
- [ ] Server haben keinen Zugang zu den Clients (kein Zugriff auf localhost)
  - mussten wir auch Clients in devcontainer Umgebung einbeziehen
  - musste man gateway einbauen (keine Ahnung wie)
  - brauchen wir es zum Gluck nicht

# Dev container

## debugging
- [x] works in VSCode with java extension pack


# TODO
- [ ] in intellij testen
  - [ ] devcontainers in intellij sind echt heavy-weight
  - [ ] es wäre, es schwer auf Laptops zu entwickeln
  - beschreib in Notizen, dass dev containers in intellij sehr heavy-weight sind


# Fragen
- [x] docker oder etwas anderes?
  - [x] gibt's eine einfachere Möglichkeit, zu entwickeln
  - lokal (ein PC) - ein spread demon
  - verteilt (mehere PCs) - ein Spread demon pro PC
- [x] ohne dev container kann man sich mit dem Netzwerk nicht verbinden
  - [x] wie könnte man ein "lokales" Netzwerk erstellen?
  - einfach ein lokales spread demon

# Probleme
- [x] Container stoppen 
  - Wenn ich einen Container stoppe, werden die anderen noch einmal gestartet 
  - Es ist komisch
  - letztendlich benutzen wir keine Container

