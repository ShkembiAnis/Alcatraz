# Ubunut
FROM ubuntu:latest

# add 32 bit architecure support
RUN dpkg --add-architecture i386
RUN apt update
RUN apt install -y libc6:i386 libstdc++6:i386

# copy spread files
WORKDIR /spread_bin
COPY ./deps/spread-bin-4.0.0/ .

# install spread
RUN apt install -y make
RUN apt install -y binutils
RUN make install

# install java
RUN apt update && \
  apt install -y openjdk-21-jdk

# add user and switch to it
RUN useradd -ms /bin/bash alcatraz
USER alcatraz

# copy project files
WORKDIR /server
COPY ./server ./server
COPY ./Makefile ./Makefile
COPY ./spread.conf ./spread.conf

# CMD [ "spread & make exec" ]
CMD spread -c ./spread.conf & make exec


# Alpine - more suitable starting image for docker
# FROM alpine:latest

# # install java
# RUN apk update && \
#   apk add --no-cache openjdk21 && \
#   apk add --no-cache make && \
#   apk add --no-cache file

# RUN apk add libc6-compat
# RUN apk add gcompat


# # install spread
# WORKDIR /spread_bin
# COPY ./deps/spread-bin-4.0.0/ .

# # RUN chmod +x ./spread

# CMD [ "ls" ]

# WORKDIR /server

# COPY ./lib ./lib
# COPY ./server ./server

# RUN javac -cp ./lib/spread-4.0.0-api.jar -d ./target ./server/Main.java
# CMD java -cp ./target:./lib/spread-4.0.0-api.jar server.Main