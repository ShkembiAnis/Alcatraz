
# [x] 1. Registering a player with the same name on different servers
1. [x] S1 - ON
2. [x] C1 - ON
3. [x] C1 - enter name - "1"
4. [x] S2 - ON
5. [x] S1 - OFF
6. [x] C2 - ON
7. [x] C2 - entry name - "1"
8. [x] "1 already exists"



# KNOWN ISSUES

## POSSIBILITY OF LOSING THE STATE OF THE GAME!

### Situation
1. server3 (ID = 3) is primary
2. server2 (ID = 2 < 3) is down
3. server2 is up and joins a group
4. server2 sees itself as primary (has the lowest ID in the group(2, 3))
   - but server3 sees itself also as primary (is still before the new election)
5. the old primary (server3) still didn't send the current state of the game
6. player1 sends a message to server2
7. server2 processes the message since it sees itself as primary
   - and tries to replicate it
   - it means it tries to replicate a state that does not consider the state of the game before server2 joined the group
