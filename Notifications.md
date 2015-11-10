There are a few parameters which determine is a notification should be sent or not:
  * **group** - indicates a group for a notifications set. This is just logical separation and takes affect only for UI.
  * **series** - indicates a series for a notifications set. Only last notification from the series will be sent to player after session timeout. For example, if there is more that one 'Your turn notification' when only last one in queue will be sent or first one if player online.
  * **even online** - indicates should be notification sent even the player online at this moment. If this parameter is false and player is online the notification will be putted into a queue and if there is no more player activity before session timeout when notification will be sent at the end of the session; otherwise the notification will be dropped.
  * **Enabled by default** - indicates is a notification enabled by default or not.

| **Name** | **Group**| **Series** | **Enabled by default** | **Even online** |
|:---------|:---------|:-----------|:-----------------------|:----------------|
| game.state.started | games    |            | true                   | false           |
| game.state.finished | games    |            | true                   | true            |
| game.move.your | games    | game.move  | true                   | false           |
| game.move.opponent | games    | game.move  | false                  | false           |
| game.timeout.day | expiration |            | true                   | false           |
| game.timeout.half | expiration |            | true                   | false           |
| game.timeout.hour| expiration |            | true                   | true            |
| game.challenge.received | challenge | game.challenge | true                   | false           |
| game.challenge.rejected | challenge | game.challenge | false                  | false           |
| game.message | message  | game.message | true                   | false           |