# Alcatraz

JDK - 21
Java - 21



# Intellij Setting
## Install the Libraries  
1. Auf ./server/lib das spread-4.0.0-api als Library hinzufügen. Rechtsclick Add as Library... in Project Level  
2. Auf ./client/lib das alcatraz-lib als Library hinzufügen. Rechtsclick Add as Library... in Project Level

![add external library in Intellij](https://github.com/user-attachments/assets/b495a506-bc04-4c5c-a5d0-b24f61a7e30a)

## Add Configuration for multiple Server.
![grafik](https://github.com/user-attachments/assets/d6a06466-810f-4b44-8df3-c8bf0bc7dfac)
1. Edit Configurations
2. Create 3 Server Configurations
3. Set in the every of them different **Program arguments**:

  - `-id 1 -s localhost -sp 4803 -g ServerGroup`
  - `-id 2 -s localhost -sp 4803 -g ServerGroup`
  - `-id 3 -s localhost -sp 4803 -g ServerGroup`

# HOW TO RUN?
0. Go to root project directory

1. Install alcatraz-lib and spread in your local maven repository

```shell
mvn install:install-file -Dfile=./client/lib/alcatraz-lib.jar -DgroupId=alcatraz-lib -DartifactId=alcatraz-lib -Dversion=1.0 -Dpackaging=jar
```
  
```shell
mvn install:install-file -Dfile=./server/lib/spread-4.0.0-api.jar -DgroupId=spread -DartifactId=spread -Dversion=1.0 -Dpackaging=jar
```

2. Build the whole project
```shell
mvn clean install
```

3. spread.conf setting
- set the setting in `spread-bin-4.0.0/bin/win32/spread.conf`
- replace the values in `<>` with your correct values (can get it using `ip a`/`ipconfig`)
```
Spread_Segment <MULTICAST_ADDRESS_OF_YOUR_NETWORK>:4803 {
    <YOUR_NAME>		<IP_ADDRESS_OF_YOUR_COMPUTER_IN_YOUR_LAN>
}

```

4. Run spread
- execute `spread-bin-4.0.0/bin/win32/spread.exe`

5. Add RMI Settings in **root folder of the project** with name `rmi.json`:
```json
{
  "1": {
    "ip": "localhost",
    "port": 1099
  },
  "2": {
    "ip": "localhost",
    "port": 1100
  },
  "3": {
    "ip": "localhost",
    "port": 1101
  }
}
```

5. Run the server
```shell
java -jar ./server/target/server-0.0.1.jar
```

6. Run the client
```shell
java -jar ./client/target/client-0.0.1.jar
```

# Client - Interfaces 

# startGame(lobbyId Long, List <clients>); Boolean

description: When the lobby owner initiates the start of the game, the server invokes the other clients and distributes a list of the clients as well as an id of the lobby

lobbyId Long: the identifiere of the lobby 
List <clients>: a list of clients, a client hold the following attributes: ip_adresse, 

@return - a Boolean which tells us

# doMove(playerName String, move Move); Boolean

description: this function proccess the move but doesn't (execute) the move. It check if the move is possible. 

playerName String: Player which made the current move

move Move: the Move don  by the Player

@return: If the move is possible and legal - return true else return false,

# nextPlayer() void

description: When all other players acknowledged the move of the current player. the nextPlayer function is triggerd and executes the move which was played in the "doMove" function
