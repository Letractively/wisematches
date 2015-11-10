

# Introduction #

This page contains information about AJAX API for a scribble board between client and server, URLs, structures, parameters and responses.

**NOTE**: all data between client and server must be in JSON format.

# Structures #
## TILE ##
| **field** | **type** | **description** |
|:----------|:---------|:----------------|
| number    | int      | number of the tile in a bank |
| letter    | char     | the letter      |
| cost      | int      | the tile's cost |
| wildcard  | boolean  | indicates is tile wildcard and can be replaced to any other |

## POSITION ##
| **field** | **type** | **description** |
|:----------|:---------|:----------------|
| row       | int      | row number starting with zero |
| column    | int      | column number starting with zero |

## WORD ##
| **field** | **type** | **description** |
|:----------|:---------|:----------------|
| tiles     | _TILE_`[]` | array of tiles  |
| position  | _POSITION_ | the start position of the word |
| direction | string   | the word's direction. Possible values: _VERTICAL_ or _HORIZONTAL_  |
| text      | string   | the word as a simple string |

## HAND ##
| **field** | **type** | **description** |
|:----------|:---------|:----------------|
| playerId  | long     | the id of the player |
| points    | short    | current player's score |
| tiles     | _TILE_`[]` | array of tiles in the player's hand |

## MOVE ##
| **field** | **type** | **description** |
|:----------|:---------|:----------------|
| number    | short    | the move's number |
| points    | short    | points for the move |
| player    | long     | the player id who made the move |
| timeMillis | long     | time in milliseconds in GMT timezone |
| timeMessage | string   | common string date representation |
| type      | string   | Type of the move. Possible value: _pass_, _make_ and _exchange_ |
| word      | _WORD_   | the word. **Only for _make_ type** |
| tilesCount | int      | the exchanged tiles count. **Only for _exchange_ type** |

## COMMENT ##
| **field** | **type** | **description** |
|:----------|:---------|:----------------|
| id        | long     | the id of the comment |
| test      | string   | text of the comment |
| person    | long     | the id of the player who made the comment |
| elapse    | string   | time elapsed since last load |

## STATE ##
| **field** | **type** | **description** |
|:----------|:---------|:----------------|
| active    | boolean  | indicates is game still active or not |
| playerTurn | long     | player's id who owns the turn or made last turn for not active game |
| spentTimeMillis | long     | the time in milliseconds that was spent to this game |
| spentTimeMessage | string   | the human readable time that was spent to this game  |
| **If game is active** |
| remainedTimeMillis | long     | remained time before timeout in milliseconds |
| remainedTimeMessage| string   | remained time before timeout in common string format |
| **If game is not active** |
| winners   | long`[]` | array of the player ids who won the game |
| resolution | string   | the game resolution. Possible values: _FINISHED_, _STALEMATE_, _TIMEOUT_ and _RESIGNED_ |
| finishTimeMillis | long     | the time in milliseconds when the game was finished |
| finishTimeMessage | string   | the human readable time when the game was finished  |
| ratings   | _RATING_`[]` | **Only for inactive game**. Array of rating changes for all players |

## PLAYER ##
| **field** | **type** | **description** |
|:----------|:---------|:----------------|
| id        | long     | the player id   |
| points    | int      | the current points |
| index     | int      | the player's index |
| nickname  | string   | the nickname    |
| membership | string   | the player's membership. Possible values: _GUEST_, _ROBOT_, _BASIC_, _SILVER_, _GOLD_ and _PLATINUM_ |
| if game has been finished: |
| rating    | int      | rating of the player after the game (not current) |
| ratingDelta | int      | delta between before and after game ratings |

## BONUS ##
| **field** | **type** | **description** |
|:----------|:---------|:----------------|
| row       | int      | the bonus row position |
| column    | int      | the bonus column position |
| type      | string   | the bonus type. Possible values: 2l, 2w, 3l, 3w |

## TILEINFO ##
| **field** | **type** | **description** |
|:----------|:---------|:----------------|
| letter    | int      | the tile's letter |
| cost      | int      | the tile's cost |
| count     | string   | the number of such tiles in the bank |

## RATING ##
| **field** | **type** | **description** |
|:----------|:---------|:----------------|
| playerId  | long     |                 |
| boardId   | long     |                 |
| Date      | changeDate |                 |
| oldRating | short    |                 |
| newRating | short    |                 |
| ratingDelta | short    |                 |
| points    | short    |                 |

## SCRIBBLEGAME ##
| **field** | **type** | **description** |
|:----------|:---------|:----------------|
| id        | long     | the game id     |
| daysPerMove | int      | number of days per move |
| state     | _STATE_  | the sate description for the game. |
| players   | _PLAYER_`[]` | array of players where key is 'player' string plus player's id |
| ratings   | _RATING_`[]` | **Only for inactive game**. Array of rating changes for all players |
| board.bonuses | _BONUS_`[]` | The list of bonuses |
| board.moves | _MOVE_`[]` | list of all done moves |
| bank.capacity | int      | the bank capacity |
| bank.tilesInfo | _TILEINFO_`[]` | the list of information about bank tiles |
| privacy.handTiles | _TILE_`[]` | **Only for active game where player in the game**. Tiles in the player's hand. |

# AJAX Requests #
## Common info ##
Any AJAX request returns structure with three parameters:
  * _success_ - boolean value that contains true if action has been done; false - if active can't be executed due internal issue or incorrect input parameters.
  * _summary_ - string value that contains common description. Usually is used of request is not success.
  * _data_ - additional information. Can be null or contains any other structures or values.

## Make Turn / Pass Turn / Exchange Tiles / Load Changes ##
**URLs**:
| Make Turn | /game/playboard/make.ajax |
|:----------|:--------------------------|
| Pass Turn | /game/playboard/pass.ajax |
| Exchange Tiles | /game/playboard/exchange.ajax |
| Load Changes | /game/playboard/changes.ajax |

**URL Parameters**:
| b | long | the game id | for all requests |
|:--|:-----|:------------|:-----------------|
| m | short | number of current moves | for load changes only |
| c | short | number of current comments | for load changes only |

**Data**
| Make Turn | word | _WORD_ | the _WORD_ structure with a move | for make turn only |
|:----------|:-----|:-------|:---------------------------------|:-------------------|
| Exchange Tiles | tiles | _TILE_`[]` | the array of tiles to be replaced | for exchange tiles only |

**Response**:
> in case of error _subject_ field contains localized information about the issue. Otherwise the _data_ is the following structure:
| state | _STATE_ | contains information about the game state |
|:------|:--------|:------------------------------------------|
| moves | _MOVE_`[]` | contains information about the move       |
| hand  | _TILE_`[]` | contains tiles that now in player's hand. |
| comments | _COMMENT_`[]` | contains array of new comments.           |
| **if game is finished** |
| players | _PLAYER_`[]` | list of player hands with final points and ratings |

## Resign Game ##
**URL**: /game/playboard/resign.ajax

**Parameters**:
| gameId | long | the game id |
|:-------|:-----|:------------|

**Response**:
> in case of error _subject_ field contains localized information about the issue. Otherwise the _data_ is the following structure:
| state | _STATE_ | contains information about the game state |
|:------|:--------|:------------------------------------------|
| **if game is finished** |
| players | _PLAYER_`[]` | list of player hands with final points and ratings |

## Check a Word ##
**URL**: /game/playboard/check.ajax

**Parameters**:
| word | string | the word |
|:-----|:-------|:---------|
| lang | string | the language |

**Response**:
> Only _success_ flag is used in response.