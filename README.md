# Corona Vs. Humanity

---

## General Premise
	Coronavirus disease (SARS-CoV-2), a newly discovered infectious disease, has descended to Earth from Jupiter. It spreads quickly by coughing or sneezing which will give the infectees a serious respiratory illness. All human beings have escaped the Earth to prevent the contagious infection and to spread the virus quickly, except for one brave doctor. She believes that there is no such place better than Earth. Therefore, she is willing to stand up for it and save the world from heinous coronoids. The stakes are high―she is defending all of humanity with only an assortment of syringes at her disposal. Can she save the world from the coronoids?


---

## Game format
	The doctor is at the bottom of the screen. Below her lies planet Earth, which she aims to protect. The mean coronoids descend from the top of the screen-- they are trying to reach the humans and infect them. The doctor thus aims to destroy the coronoids before they can reach the defenseless humans, armed with syringe projectiles that she can launch directly upward. Once the coronoids get shot by the doctor, the explosion occurs which automatically removes coronoids from the screen. When the coronoids get eliminated, the player’s score increases and coronoids level up as the score increases which develops higher speed and agility. The game contains a scoreboard on the screen to keep track of the player’s points. This game ends when the player is touched by the enemy and gives the option to start over again.


---

## Implementation Overview
	JavaFX was the chosen framework to develop the game. A moving object class is the parent class for the player ‘Nurse’ character and the ‘Coronoid’ enemies. A shot class represents projectiles. These classes store an image and a position and detect collision with other objects. The main engine which drives the game forward in time is a JavaFX.animation.Timeline object which cycles indefinitely through the method run(), using a JavaFX.scene.graphics. GraphicsContext to contain all the animated components. Every time our timeline cycles through the run(), each object on the screen is updated and redrawn. The player’s nurse character updates position by tracking the user’s mouse movement. Coronoids are set by default to move downward at a pace that scales with the user’s score, getting more difficult to hold back over time. The user may shoot with the mouse’s left click, firing a projectile that moves directly upward. The MAX_SHOTS constant limits the number of projectiles the player can fire at a time, and the game ensures this by keeping track of every shot currently on screen in a List<Shot>. If a coronoid is shot, boolean ‘exploding’ is set to true, which triggers the character to be replaced with a fire image which lasts 15 (EXPLOSION_STEPS) frames - after which a boolean destroyed is set to true, cueing the game to erase and replace it with a new Coronoid generated from the top of the screen. As soon as a virus dies, it is promptly replaced, keeping the number of viruses at a constant level determined by the MAX_VIRUS constant. If the player itself collides with a Coronoid, they also explode and the game ends, prompting the user to restart if they desire.


---

## Demo
![Corona Vs Humanity Demo](demo/demo.gif)

