```
CREATE TABLE players (
uuid CHAR(36) PRIMARY KEY
);
```
```
CREATE TABLE friends (
player_uuid CHAR(36) NOT NULL,
friend_uuid CHAR(36) NOT NULL,
PRIMARY KEY (player_uuid, friend_uuid),
FOREIGN KEY (player_uuid) REFERENCES players(uuid),
FOREIGN KEY (friend_uuid) REFERENCES players(uuid)
);```