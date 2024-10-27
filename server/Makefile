compile:
	javac -cp ./lib/spread-4.0.0-api.jar -d ./target ./server/Main.java

run:
	java -cp ./target:./lib/spread-4.0.0-api.jar server.Main

exec: compile run

doc-build:
	docker build -t alcatraz-server .

doc-run:
	docker run alcatraz-server

doc-img-interactive:
	docker run -it alcatraz-server /bin/bash

doc-exec: doc-build doc-run

doc-it: doc-build doc-interactive
