# Main Idea #
We have three things:
  * Letters in the robot's hand
  * Letters on the board
  * Dictionary with known words

Scrabble Robot iterates all words in dictionary and makes list of all valid moves for each of them.
After that robot can select word for next turn from this list according to it's type:
  1. Robot Dull just selects random word from list. But doesn't use words longer than six letters.
  1. Robot Fine selects the most long word. But doesn't use words longer than eight letters.
  1. Robot Expert selects the most expensive word.

To do this work for each word robot creates tree of all allowed horizontal and vertical moves.

# Example #
For example, we have the following board:
```
 ----------------------
 |__|___| c |___|___|
 ----------------------
 |__|___| o |___|___|
 ----------------------
 | h | e| l | l | o |
 ----------------------
```

Robot has letters 'abcdo' in it's hand.

Now we starting check all words in dictionary. For example in dictionary we have following words:
```
aback
doc
```

Now we a creating list of all letters for first char of word: 'a'. There is only one letter — in robot's hand. It means that we theoretically can create this word. Now we are creating tree with the root letter 'a'. We a taking next char — 'b' and we have only one letter in the robot's hand again. So our tree will look as following:
```
       a (hand)
       |
       b (hand)
```
When we are taking next char 'a'.  Because  we don't have any letters of char 'a' (char from hand we already using as start char) we stop search and move to next word.

Now we a taking word 'doc'.
We are taking first char from hand. We have three chars 'o': on the board in position 23 (2 — row, 3 — column) and position 35 (3 — row, 5 — column) and one in hand.  And we have two chars 'c': one on the board in position 13 and one in the hand. So our tree will look like:
```
	    d(hand)
          /   |    \
      o(23) o(35)  o(hand)
       /         /   \
  c(hand)    c(hand)  c(13)      
```
After that we can select all valid position of word 'doc': d(hand) o(23) c(hand); d(hand) o(hand) c(13). Word d(hand) o(hand) c(hand) isn't valid because we must use at least one char from board.

_Note_: during making a tree we also check that position of word is valid. When next char from board we must check that all previous chars can be placed on the board without conflicts.

# Optimization #
This algorithm isn't optimized. We can optimize it using:
  1. Now horizontal and vertical search are separated.  Its must be integrated.
  1. We don't have check all words that start with chars that can't be placed (if we can't place word 'doc' we mustn't check words: document, documentation and so on.