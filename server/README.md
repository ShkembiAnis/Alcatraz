# Requirements
- docker
- make
- java


# HOW TO RUN?
- look into the `./Makefile` to see details of the commands

## without docker
`make exec`

## single docker container
`make doc-exec`

## collection of servers
`make doc-collection`

### run test server container with a delay
`docker exec s_test make exec`

# HOW TO STOP a container?
`docker container stop <container_name>`
