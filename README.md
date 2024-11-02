# Alcatraz

JDK - 21
Java - 21



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