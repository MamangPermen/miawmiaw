# Strike Vampcats (Miaw Miaw Boom) 🐱💥

**Strike Vampcats** is a Top-Down Survival Shooter game built with Java Swing. Survive endless waves of enemies, dodge obstacles, manage your ammo.

## ✨ Features

* **Strict MVP Architecture:** Clean separation of concerns using Interfaces (Contract-based programming) for Views and Presenters.
* **Leaderboard System:** Persistent data storage using MySQL Database (`db_tmd`).
* **Dynamic Gameplay:**
    * Enemy AI with pathfinding logic (sliding around obstacles).
    * Ammo management system (Missed shots cost ammo, enemy misses grant ammo).
    * Obstacle generation with collision detection.
* **Custom UI:** Pixel-art style interface, custom buttons, and smooth scrolling leaderboard.
* **Audio System:** Background music (BGM) and Sound Effects (SFX) for shooting and hits.

## 🛠️ Tech Stack & Architecture

This project demonstrates a **Strict Model-View-Presenter (MVP)** pattern to ensure loose coupling and high testability.

* **Model:** Handles business logic, game state, database operations (`Database.java`, `Player`, `Enemy`).
* **View:** Passive interface responsible only for rendering and capturing input (`GamePanel`, `MainMenu`). Defines strict contracts via Interfaces (`IGamePanel`, `IMainMenu`).
* **Presenter:** The middleman that retrieves data from the Model and updates the View (`GamePresenter`, `MainMenuPresenter`).

## 🚀 How to Run
Prerequisites
Java Development Kit (JDK) 8 or higher.

MySQL Server installed and running.

### 1. Database Setup
Execute the following SQL command in your MySQL client to set up the database:

```sql
CREATE DATABASE db_tmd;

USE db_tmd;

CREATE TABLE tbenefit (
    username VARCHAR(50) PRIMARY KEY,
    skor INT DEFAULT 0,
    missed_bullets INT DEFAULT 0,
    ammo INT DEFAULT 0
);
```

### 2. Compile & Launch
```bash
# Create bin directory & copy assets
mkdir bin
xcopy src\assets bin\assets /s /i /y

# Compile Java files
javac -d bin -cp "src/lib/*" src/model/*.java src/view/*.java src/presenter/*.java src/Main.java

# Launch the Game
java -cp "bin;src/lib/*" Main
```

## 🎮 Controls
Key / Action,Function
W / Up Arrow,Move Up
S / Down Arrow,Move Down
A / Left Arrow,Move Left
D / Right Arrow,Move Right
Mouse Click,Shoot
Space,Pause Game