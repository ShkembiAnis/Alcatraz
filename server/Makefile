# RUN without docker
compile:
	javac -cp ./lib/spread-4.0.0-api.jar -d ./target ./server/Main.java

run:
	java -cp ./target:./lib/spread-4.0.0-api.jar server.Main

exec: compile run

# RUN single docker container
doc-build:
	docker build -t alcatraz-server .

doc-run:
	docker run alcatraz-server

doc-exec: doc-build doc-run

doc-img-interactive:
	docker run -it alcatraz-server /bin/bash

doc-it: doc-build doc-interactive

# RUN a collection of servers
doc-collection:
	docker compose up --build


# run spread
spread:
	spread -c ./spread.conf > /dev/null 2>&1 &