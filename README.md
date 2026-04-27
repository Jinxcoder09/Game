# Jungle Strike: Survivor

A fast-paced 2D Python action game built with Pygame.

## What Was Upgraded
This project has been transformed from early prototype files into a playable action game loop with:

- Real-time player movement and aiming (`WASD`/arrow keys + mouse).
- Shooting system with projectile collision and enemy damage.
- Multiple enemy waves with increasing difficulty.
- 3 full levels with progression, weapon upgrades, and healing between stages.
- HUD with health, weapon, score, and level info.
- Game states: menu, paused, level clear, game over, and victory.
- Procedural retro-style sound effects (shoot, hit, level clear, victory, game over).

## Controls
- **Move**: `W A S D` or arrow keys
- **Shoot**: Left mouse click
- **Start game**: `Enter`
- **Pause / Resume**: `P`
- **Next level** (after clear): `Space`
- **Restart** (after game over/victory): `R`

## Run
```bash
pip install pygame
python jungle_survival.py
```

## Game Objective
Survive all enemy waves across 3 jungle sectors and clear the final level.

## Technical Notes
- No external assets are required.
- Graphics are rendered directly using Pygame.
- Sound is generated at runtime using Pygame's mixer module.
