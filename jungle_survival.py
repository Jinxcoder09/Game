"""Jungle Strike: Survivor - A 2D action survival game using Pygame."""

import pygame
import math
import random
import threading
import struct
import wave
import io
from enum import Enum
from dataclasses import dataclass, field
from typing import List, Optional, Tuple

# Initialize Pygame
pygame.init()
pygame.mixer.init(frequency=44100, size=-8, channels=1)


class GameState(Enum):
    MENU = "menu"
    PLAYING = "playing"
    LEVEL_COMPLETE = "level_complete"
    GAME_OVER = "game_over"
    VICTORY = "victory"
    PAUSED = "paused"


@dataclass
class Level:
    level_number: int
    name: str
    enemy_count: int
    enemy_speed: float
    enemy_health: int
    spawn_interval_ticks: int
    sky_tint: Tuple[int, int, int]


@dataclass
class Weapon:
    name: str
    damage: int
    cooldown_ms: int
    projectile_speed: float
    projectile_radius: int


@dataclass
class Projectile:
    x: float
    y: float
    vx: float
    vy: float
    damage: int
    radius: int

    def step(self) -> 'Projectile':
        return Projectile(
            self.x + self.vx,
            self.y + self.vy,
            self.vx,
            self.vy,
            self.damage,
            self.radius
        )


class Player:
    def __init__(self, x: float, y: float, width: int, height: int):
        self.x = x
        self.y = y
        self.size = 28
        self.speed = 5.0
        self.max_health = 100
        self.health = self.max_health
        self.weapon = Weapon("Scout Blaster", 12, 180, 8.0, 4)
        self.last_shot_at = 0
        self.width = width
        self.height = height

    def move(self, dx: int, dy: int):
        length = math.sqrt(dx * dx + dy * dy)
        if length == 0:
            return

        self.x += (dx / length) * self.speed
        self.y += (dy / length) * self.speed

        self.x = max(self.size, min(self.width - self.size, self.x))
        self.y = max(self.size, min(self.height - self.size, self.y))

    def can_shoot(self, now_ms: int) -> bool:
        return now_ms - self.last_shot_at >= self.weapon.cooldown_ms

    def shoot(self, target_x: float, target_y: float, now_ms: int) -> Projectile:
        self.last_shot_at = now_ms
        dx = target_x - self.x
        dy = target_y - self.y
        length = math.sqrt(dx * dx + dy * dy)
        if length == 0:
            length = 1

        return Projectile(
            self.x,
            self.y,
            (dx / length) * self.weapon.projectile_speed,
            (dy / length) * self.weapon.projectile_speed,
            self.weapon.damage,
            self.weapon.projectile_radius
        )

    def heal(self, value: int):
        self.health = min(self.max_health, self.health + value)

    def take_damage(self, value: int):
        self.health = max(0, self.health - value)

    def upgrade_weapon(self, weapon: Weapon):
        self.weapon = weapon


class Animal:
    def __init__(self, animal_type: str, x: float, y: float, size: int, 
                 speed: float, health: int, contact_damage: int):
        self.type = animal_type
        self.x = x
        self.y = y
        self.size = size
        self.speed = speed
        self.health = health
        self.contact_damage = contact_damage

    def update(self, player: Player):
        dx = player.x - self.x
        dy = player.y - self.y
        length = math.sqrt(dx * dx + dy * dy)
        if length == 0:
            return

        self.x += (dx / length) * self.speed
        self.y += (dy / length) * self.speed

    def contains_point(self, px: float, py: float) -> bool:
        dx = px - self.x
        dy = py - self.y
        return math.sqrt(dx * dx + dy * dy) <= self.size * 0.5

    def collides_with_player(self, player: Player) -> bool:
        dx = player.x - self.x
        dy = player.y - self.y
        return math.sqrt(dx * dx + dy * dy) <= player.size * 0.5 + self.size * 0.5

    def take_damage(self, value: int):
        self.health -= value

    @property
    def is_dead(self) -> bool:
        return self.health <= 0


class SoundEngine:
    def __init__(self):
        pass

    def play_tone(self, hz: int, msecs: int, volume: float):
        """Play a tone using pygame mixer."""
        def generate_and_play():
            try:
                sample_rate = 44100
                n_samples = int(sample_rate * msecs / 1000)
                samples = []
                
                for i in range(n_samples):
                    angle = i / (sample_rate / hz) * 2.0 * math.pi
                    sample = int(math.sin(angle) * 127.0 * volume)
                    samples.append(max(-128, min(127, sample)))
                
                # Create sound from buffer
                sound_bytes = bytes([(s + 128) & 0xFF for s in samples])
                sound = pygame.mixer.Sound(buffer=sound_bytes)
                sound.set_volume(volume)
                sound.play()
            except Exception:
                pass  # Silent fallback
        
        thread = threading.Thread(target=generate_and_play, daemon=True)
        thread.start()

    def play_shoot(self):
        self.play_tone(880, 70, 0.2)

    def play_hit(self):
        self.play_tone(220, 100, 0.35)

    def play_level_up(self):
        self.play_tone(520, 110, 0.25)
        pygame.time.wait(50)
        self.play_tone(760, 110, 0.25)

    def play_game_over(self):
        self.play_tone(300, 200, 0.35)

    def play_victory(self):
        self.play_tone(660, 100, 0.25)
        pygame.time.wait(50)
        self.play_tone(880, 100, 0.25)
        pygame.time.wait(50)
        self.play_tone(1100, 120, 0.25)


class GraphicsEngine:
    @staticmethod
    def draw_background(screen: pygame.Surface, level: Level, width: int, 
                        height: int, tick: int):
        tint = level.sky_tint
        bg_color = (max(0, tint[0] - 25), max(0, tint[1] - 25), max(0, tint[2] - 25))
        screen.fill(bg_color)
        
        # Sky
        pygame.draw.rect(screen, tint, (0, 0, width, int(height * 0.65)))
        
        # Ground
        pygame.draw.rect(screen, (34, 120, 52), (0, int(height * 0.65), width, int(height * 0.35)))
        
        # Clouds
        cloud_color = (255, 255, 255, 90)
        cloud_surface = pygame.Surface((180, 60), pygame.SRCALPHA)
        cloud_surface.fill((*cloud_color[:3], cloud_color[3]))
        wave = (tick // 3) % width
        screen.blit(cloud_surface, (-120 + wave, 40))
        
        cloud_surface2 = pygame.Surface((210, 70), pygame.SRCALPHA)
        cloud_surface2.fill((*cloud_color[:3], cloud_color[3]))
        screen.blit(cloud_surface2, (180 + wave // 2, 85))

    @staticmethod
    def draw_player(screen: pygame.Surface, player: Player):
        x, y = int(player.x), int(player.y)
        size = player.size
        
        # Body
        pygame.draw.circle(screen, (37, 42, 52), (x, y), size // 2)
        # Inner circle
        pygame.draw.circle(screen, (83, 188, 255), (x, y), size // 3)

    @staticmethod
    def draw_enemy(screen: pygame.Surface, enemy: Animal):
        colors = {
            "Tiger": (231, 136, 45),
            "Wolf": (144, 145, 158),
            "Alpha": (120, 65, 45)
        }
        enemy_color = colors.get(enemy.type, (120, 65, 45))
        
        x, y = int(enemy.x), int(enemy.y)
        size = enemy.size
        
        # Body
        pygame.draw.circle(screen, enemy_color, (x, y), size // 2)
        
        # Eyes
        pygame.draw.circle(screen, (0, 0, 0), (x - 4, y - 2), 3)
        pygame.draw.circle(screen, (0, 0, 0), (x + 1, y - 2), 3)
        
        # Health bar background
        bar_width = size
        bar_height = 5
        bar_x = x - size // 2
        bar_y = y - size // 2 - 8
        pygame.draw.rect(screen, (0, 0, 0, 90), (bar_x, bar_y, bar_width, bar_height))
        
        # Health bar foreground
        health_width = max(0, min(size, enemy.health * size // 60))
        pygame.draw.rect(screen, (235, 70, 70), (bar_x, bar_y, health_width, bar_height))

    @staticmethod
    def draw_projectile(screen: pygame.Surface, projectile: Projectile):
        radius = projectile.radius
        pygame.draw.circle(screen, (255, 239, 90), 
                          (int(projectile.x), int(projectile.y)), radius)

    @staticmethod
    def draw_hud(screen: pygame.Surface, player: Player, level: Level, 
                 score: int, enemies_left: int, font: pygame.font.Font):
        # Background
        hud_rect = pygame.Rect(12, 12, 340, 85)
        pygame.draw.rect(screen, (0, 0, 0, 125), hud_rect, border_radius=18)
        
        # Text
        text_color = (255, 255, 255)
        level_text = font.render(f"LEVEL {level.level_number} - {level.name}", True, text_color)
        hp_text = font.render(f"HP: {player.health}/{player.max_health}", True, text_color)
        weapon_text = font.render(f"Weapon: {player.weapon.name}", True, text_color)
        score_text = font.render(f"Score: {score}  Enemies Left: {enemies_left}", True, text_color)
        
        screen.blit(level_text, (24, 34))
        screen.blit(hp_text, (24, 54))
        screen.blit(weapon_text, (24, 74))
        screen.blit(score_text, (170, 54))

    @staticmethod
    def draw_center_message(screen: pygame.Surface, line1: str, line2: str,
                            width: int, height: int, 
                            title_font: pygame.font.Font, body_font: pygame.font.Font):
        # Background
        rect_width, rect_height = 520, 140
        rect = pygame.Rect(width // 2 - rect_width // 2, height // 2 - rect_height // 2,
                          rect_width, rect_height)
        pygame.draw.rect(screen, (0, 0, 0, 165), rect, border_radius=24)
        
        # Line 1
        text1 = title_font.render(line1, True, (255, 255, 255))
        text1_rect = text1.get_rect(center=(width // 2, height // 2 - 10))
        screen.blit(text1, text1_rect)
        
        # Line 2
        text2 = body_font.render(line2, True, (255, 255, 255))
        text2_rect = text2.get_rect(center=(width // 2, height // 2 + 24))
        screen.blit(text2, text2_rect)

    @staticmethod
    def draw_crosshair(screen: pygame.Surface, mouse_x: int, mouse_y: int):
        color = (255, 255, 255, 190)
        # Circle
        pygame.draw.circle(screen, color, (mouse_x, mouse_y), 12, 2)
        # Lines
        pygame.draw.line(screen, color, (mouse_x - 18, mouse_y), (mouse_x + 18, mouse_y), 2)
        pygame.draw.line(screen, color, (mouse_x, mouse_y - 18), (mouse_x, mouse_y + 18), 2)


class GamePanel:
    WIDTH = 1024
    HEIGHT = 640

    def __init__(self):
        self.screen = pygame.display.set_mode((self.WIDTH, self.HEIGHT))
        pygame.display.set_caption("Jungle Strike: Survivor")
        self.clock = pygame.time.Clock()
        self.font = pygame.font.Font(None, 24)
        self.title_font = pygame.font.Font(None, 36)
        self.body_font = pygame.font.Font(None, 28)
        
        self.random = random.Random()
        self.sound_engine = SoundEngine()
        
        self.levels = [
            Level(1, "Rainforest Outskirts", 16, 1.1, 30, 40, (76, 158, 92)),
            Level(2, "Fog Basin", 22, 1.4, 45, 30, (80, 117, 145)),
            Level(3, "Ruins of the Apex", 30, 1.9, 60, 22, (132, 96, 162))
        ]
        
        self.enemies: List[Animal] = []
        self.projectiles: List[Projectile] = []
        self.keys = {}
        
        self.player: Optional[Player] = None
        self.game_state = GameState.MENU
        self.level_index = 0
        self.enemies_spawned = 0
        self.score = 0
        self.tick = 0
        self.mouse_x = 0
        self.mouse_y = 0
        self.damage_cooldown = 0
        
        self.reset_to_menu()

    def reset_to_menu(self):
        self.game_state = GameState.MENU
        self.player = Player(self.WIDTH / 2.0, self.HEIGHT / 2.0, self.WIDTH, self.HEIGHT)
        self.enemies.clear()
        self.projectiles.clear()
        self.level_index = 0
        self.enemies_spawned = 0
        self.score = 0
        self.tick = 0
        self.damage_cooldown = 0

    def start_game(self):
        self.reset_to_menu()
        self.game_state = GameState.PLAYING
        self.begin_current_level()

    def begin_current_level(self):
        self.enemies.clear()
        self.projectiles.clear()
        self.enemies_spawned = 0

    def begin_next_level(self):
        if self.level_index >= len(self.levels) - 1:
            self.game_state = GameState.VICTORY
            self.sound_engine.play_victory()
            return

        self.level_index += 1
        if self.level_index == 1:
            self.player.upgrade_weapon(Weapon("Predator Rifle", 18, 140, 9.2, 4))
        elif self.level_index == 2:
            self.player.upgrade_weapon(Weapon("Apex Cannon", 24, 120, 10.4, 5))
        
        self.player.heal(20)
        self.game_state = GameState.PLAYING
        self.begin_current_level()
        self.sound_engine.play_level_up()

    def spawn_enemy(self, level: Level) -> Animal:
        side = self.random.randint(0, 3)
        if side == 0:
            x = self.random.randint(0, self.WIDTH)
            y = 0
        elif side == 1:
            x = self.random.randint(0, self.WIDTH)
            y = self.HEIGHT
        elif side == 2:
            x = 0
            y = self.random.randint(0, self.HEIGHT)
        else:
            x = self.WIDTH
            y = self.random.randint(0, self.HEIGHT)

        if level.level_number == 1:
            animal_type = "Wolf"
        elif level.level_number == 2:
            animal_type = "Tiger"
        else:
            animal_type = "Alpha"
        
        size = 26 + level.level_number * 3
        damage = 8 + level.level_number * 2
        
        return Animal(animal_type, x, y, size, level.enemy_speed, level.enemy_health, damage)

    def handle_movement(self):
        dx = 0
        dy = 0
        if self.keys.get(pygame.K_w) or self.keys.get(pygame.K_UP):
            dy -= 1
        if self.keys.get(pygame.K_s) or self.keys.get(pygame.K_DOWN):
            dy += 1
        if self.keys.get(pygame.K_a) or self.keys.get(pygame.K_LEFT):
            dx -= 1
        if self.keys.get(pygame.K_d) or self.keys.get(pygame.K_RIGHT):
            dx += 1
        self.player.move(dx, dy)

    def update_projectiles(self):
        self.projectiles = [p.step() for p in self.projectiles]
        self.projectiles = [
            p for p in self.projectiles 
            if 0 <= p.x <= self.WIDTH and 0 <= p.y <= self.HEIGHT
        ]

    def handle_collisions(self):
        projectiles_to_remove = []
        
        for projectile in self.projectiles:
            hit = False
            for enemy in self.enemies:
                if enemy.contains_point(projectile.x, projectile.y):
                    enemy.take_damage(projectile.damage)
                    projectiles_to_remove.append(projectile)
                    hit = True
                    self.sound_engine.play_hit()
                    if enemy.is_dead:
                        self.score += 100
                    break
            
            if hit:
                break
        
        # Remove hit projectiles
        for p in projectiles_to_remove:
            if p in self.projectiles:
                self.projectiles.remove(p)
        
        # Remove dead enemies
        self.enemies = [e for e in self.enemies if not e.is_dead]
        
        # Player collision
        if self.damage_cooldown == 0:
            for enemy in self.enemies:
                if enemy.collides_with_player(self.player):
                    self.player.take_damage(enemy.contact_damage)
                    self.damage_cooldown = 25
                    break

    def update_playing_state(self):
        level = self.levels[self.level_index]
        self.handle_movement()

        if self.enemies_spawned < level.enemy_count and self.tick % level.spawn_interval_ticks == 0:
            self.enemies.append(self.spawn_enemy(level))
            self.enemies_spawned += 1

        for enemy in self.enemies:
            enemy.update(self.player)

        self.update_projectiles()
        self.handle_collisions()

        if self.player.health <= 0:
            self.game_state = GameState.GAME_OVER
            self.sound_engine.play_game_over()

        enemies_left = level.enemy_count - self.enemies_spawned + len(self.enemies)
        if self.enemies_spawned >= level.enemy_count and len(self.enemies) == 0:
            self.game_state = GameState.LEVEL_COMPLETE
            self.sound_engine.play_level_up()

    def draw(self):
        current_level = self.levels[min(self.level_index, len(self.levels) - 1)]
        GraphicsEngine.draw_background(self.screen, current_level, self.WIDTH, self.HEIGHT, self.tick)

        for enemy in self.enemies:
            GraphicsEngine.draw_enemy(self.screen, enemy)

        for projectile in self.projectiles:
            GraphicsEngine.draw_projectile(self.screen, projectile)

        GraphicsEngine.draw_player(self.screen, self.player)
        GraphicsEngine.draw_crosshair(self.screen, self.mouse_x, self.mouse_y)
        
        enemies_left = max(0, current_level.enemy_count - self.enemies_spawned + len(self.enemies))
        GraphicsEngine.draw_hud(self.screen, self.player, current_level, self.score, enemies_left, self.font)

        if self.game_state == GameState.MENU:
            GraphicsEngine.draw_center_message(
                self.screen, "JUNGLE STRIKE: SURVIVOR", 
                "Press ENTER to start. Move: WASD / Arrows, Shoot: Mouse",
                self.WIDTH, self.HEIGHT, self.title_font, self.body_font
            )
        elif self.game_state == GameState.PAUSED:
            GraphicsEngine.draw_center_message(
                self.screen, "PAUSED", "Press P to continue.",
                self.WIDTH, self.HEIGHT, self.title_font, self.body_font
            )
        elif self.game_state == GameState.LEVEL_COMPLETE:
            GraphicsEngine.draw_center_message(
                self.screen, "LEVEL CLEARED", "Press SPACE for next level.",
                self.WIDTH, self.HEIGHT, self.title_font, self.body_font
            )
        elif self.game_state == GameState.GAME_OVER:
            GraphicsEngine.draw_center_message(
                self.screen, "GAME OVER", "Press R to restart.",
                self.WIDTH, self.HEIGHT, self.title_font, self.body_font
            )
        elif self.game_state == GameState.VICTORY:
            GraphicsEngine.draw_center_message(
                self.screen, "VICTORY", 
                "You conquered all jungle sectors! Press R to replay.",
                self.WIDTH, self.HEIGHT, self.title_font, self.body_font
            )

        pygame.display.flip()

    def run(self):
        running = True
        while running:
            self.clock.tick(60)
            
            for event in pygame.event.get():
                if event.type == pygame.QUIT:
                    running = False
                elif event.type == pygame.KEYDOWN:
                    self.keys[event.key] = True
                    
                    if event.key == pygame.K_RETURN and self.game_state == GameState.MENU:
                        self.start_game()
                    elif event.key == pygame.K_r and self.game_state in [GameState.GAME_OVER, GameState.VICTORY]:
                        self.start_game()
                    elif event.key == pygame.K_p and self.game_state == GameState.PLAYING:
                        self.game_state = GameState.PAUSED
                    elif event.key == pygame.K_p and self.game_state == GameState.PAUSED:
                        self.game_state = GameState.PLAYING
                    elif event.key == pygame.K_SPACE and self.game_state == GameState.LEVEL_COMPLETE:
                        self.begin_next_level()
                
                elif event.type == pygame.KEYUP:
                    self.keys[event.key] = False
                
                elif event.type == pygame.MOUSEMOTION:
                    self.mouse_x, self.mouse_y = event.pos
                
                elif event.type == pygame.MOUSEBUTTONDOWN:
                    if event.button == 1:  # Left click
                        now_ms = pygame.time.get_ticks()
                        if self.game_state == GameState.PLAYING and self.player.can_shoot(now_ms):
                            self.projectiles.append(
                                self.player.shoot(self.mouse_x, self.mouse_y, now_ms)
                            )
                            self.sound_engine.play_shoot()
            
            self.tick += 1
            if self.damage_cooldown > 0:
                self.damage_cooldown -= 1
            
            if self.game_state == GameState.PLAYING:
                self.update_playing_state()
            
            self.draw()
        
        pygame.quit()


def main():
    game = GamePanel()
    game.run()


if __name__ == "__main__":
    main()
