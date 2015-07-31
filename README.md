#Hangman

---

###A client-server guessing game (a variant of the two-player "Hangman" game)

Develop a client-server distributed application in Java for a guessing game similar to the "Hangman" game (see a description in Wikipedia).

The server chooses a word from a dictionary, and the client (the player) tries to guess the word chosen by the server by suggesting letters (one letter at a time) or a whole word.  The client is given a certain number of attempts (failed attempts) when it may suggest a letter that does not occur in the word. If the client suggests a letter that occurs on the word, the server places the letter in all its positions in the word; otherwise the counter of allowed failed attempts is decremented. At any time the client is allowed to guess the whole word. The client wins when the client completes the word, or guesses the whole word correctly. The server wins over when the counter of allowed failed attempts reaches zero. The client and the server communicate by sending messages over a TCP connection according to the following protocol. The server should compute the total score of games using a score counter: if a client wins the score counter is incremented, if the client loses the score counter is decremented.

* If the client (player) wants to start a new game it sends a special "start game" message to the server . The server randomly chooses a word from a dictionary, sets the failed attempt counter to the number of allowed failed attempts, and replies with a message that includes a row of dashes (a current view of the word), giving the number of letters in the chosen word (e.g. "----------" for the word "programming"), and the number of allowed failed attempts (e.g. 10). 
* If the client (player) wants to suggest a letter or the whole word, it sends the letter or the guessed word to the server. The server replies as follows, depending on the word it has chosen and the client's guess.
If the client guesses the whole word correctly, the server sends a congratulation message together with the word and the total score (see below);

	* If the letter guessed by the client occurs in the word and the client has completed the entire word, the server sends a congratulation message together with the word and the total score;e
	* If the letter guessed by the client occurs in the word and the has not completed yet the whole word, the server sends to the client the current view of the word with the letter placed  in all its correct positions and the current value of the failed attempts counter. For example, if the client suggest "g" then the current view of the word is "---g------g", if the client then suggest "m" then the current view of the work becomes "---g--mm--g";
	* If the letter guessed by the client does not occur in the word or the client guesses the whole word incorrectly, the server decrements the failed attempt counter and, depending on the counter's value, sends either the current view of the word together with the value of the failed attempt counter (if the counter > 0), or a "gave over; you loose" message together with the total score (if the counter == 0).

The client G(UI) should display a current view of the word, the current value of the failed attempts counter, and the current value of the total score. 

As a source of words for the server, the server can use an online dictionary in /usr/dict/words under Solaris or Linux. The file is also available here. The file contains 25,143 words (and is used by the Unix spell command).
