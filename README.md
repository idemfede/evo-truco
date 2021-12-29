
# Truco


Truco is a card game widely played in South America
**https://www.pagat.com/put/truco.html**


This project implements a server for a Lobby API and Game API over Websocket

## Configuration

Necessary environment configuration is stored in *.env*


## Running

- To run the server

  sbt server/run

- To run the console client

  sbt client/run

There are two Main classes, corresponding to Lobby and Game clients


## API

### Lobby
Exposes a single URL

GET http://localhost:9000/join

Which opens a websocket connection. Two commands are accepted
- Joining a queue
- Leaving a queue


### Game
Exposes a single URL

GET http://localhost:9000/play?gameId={gameId}

Which opens a websocket connection and starts or joins the game identified by {gameId}. Two commands are accepted

- Playing a card
- Leaving the game