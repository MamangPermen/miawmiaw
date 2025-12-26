### Compile & Launch
```bash
# Create bin directory & copy assets
xcopy src\assets bin\assets /s /i /y

# Compile Java files
javac -d bin -cp "src/lib/*" src/model/*.java src/view/*.java src/presenter/*.java src/Main.java

# Launch the Game
java -cp "bin;src/lib/*" Main
```

## Controls
| Key / Input | Action |
| :--- | :--- |
| **W** / **Up Arrow** | Move Up |
| **S** / **Down Arrow** | Move Down |
| **A** / **Left Arrow** | Move Left |
| **D** / **Right Arrow** | Move Right |
| **Left Mouse Click** | Shoot |
| **Space** | Pause / Resume Game |

## Credits

This project uses high-quality assets from the following creators. We would like to express our gratitude for their contributions to the game development community:

### Visual Assets
* **Player, Enemy, & Enemy Bullets:** [Cat Pack (Halloween Edition)](https://toffeecraft.itch.io/cat-pack-halloween-edition) by **ToffeeCraft**.
* **Background:** [Vampus Castle Background](https://pixel-1992.itch.io/vampus-castle-free-pixel-art-side-scroller-background) by **PIXEL_1992**.
* **Player Bullets:** [Pixel Fire Asset Pack](https://devkidd.itch.io/pixel-fire-asset-pack) by **Devkidd**.
* **Obstacles (Boxes):** [Wood Set](https://nyknck.itch.io/wood-set) by **NYKNCK**.
* **User Interface (UI):** [Horror Mega Pack](https://toffeecraft.itch.io/horror-mega-pack) by **ToffeeCraft**.

### Audio & Fonts
* **Battle Music:** [Montogoronto](https://pixabay.com/id/users/montogoronto-34345685/) on Pixabay.
* **Menu Music:** [Tunetank](https://pixabay.com/id/users/tunetank-50201703/) on Pixabay.
* **Sound Effects (SFX):** [Free SFX Pack](https://kronbits.itch.io/freesfx) by **Kronbits**.
* **Typography:** [VT323 Font](https://fonts.google.com/specimen/VT323) by **Peter Hull** (Licensed under Open Font License).

---