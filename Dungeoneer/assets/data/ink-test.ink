EXTERNAL killPlayer()
EXTERNAL showGameOverScreen(won)
EXTERNAL fireTrigger(triggerName)

- Hello, would you like to hear my story?
* [Listen to the story]
    Thank you for letting me take your time.
    This world, it is not only your own.
    * * [What do you mean?]
        Only that the things you see with your eyes, are not all there is. There is yet more underneath that even.
            * * * [But how can that be true?]
                That is not something I can answer, unfortunately.
            * * * [Say nothing.]
                I see your mind has broken. I will leave you to ponder that.
                ~ fireTrigger("SHAKETRIGGER")
    * * [I understand]
        You are an enlightened one, aren't you?
        In that case, I have nothing to teach you it seems.
        ~ showGameOverScreen(true)
* [Leave instead.]
    Fare thee well, traveler. Or just die.
    ~ killPlayer()
