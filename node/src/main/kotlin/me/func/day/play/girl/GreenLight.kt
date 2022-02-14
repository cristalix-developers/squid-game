package me.func.day.play.girl

import dev.implario.bukkit.event.EventContext
import dev.implario.bukkit.event.on
import me.func.accept.AcceptLose
import me.func.accept.AcceptRoundWin
import me.func.SquidGame
import me.func.app
import me.func.day.Day
import me.func.day.PLAYER_PREPARE_DURATION
import me.func.day.misc.Bonus
import me.func.day.misc.Workers
import me.func.user.User
import me.func.util.Music
import org.bukkit.Material
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector

class GreenLight(private val game: SquidGame) : Day {

    override lateinit var fork: EventContext
    override val duration = 1 * 60 + 20
    override val description = "Стойте когда горит красный и\nпоспешите когда виден зеленый!"
    override val title = "Красный свет"

    private val spawn = game.map.getLabels("day1").map {
        val current = it.toCenterLocation().add(0.0, 1.5, 0.0)
        current.yaw = -180f
        current
    }
    private val turrets = game.map.getLabels("gun").map { Gun(it) }.associateBy { it.uuid }
    private val bonus = game.map.getLabels("boost").map { it.toCenterLocation() }
    private val girl = Girl(game.map.getLabel("girl"))
    private val effects = listOf(
        PotionEffect(PotionEffectType.SLOW, duration * 20 * 2, 2),
        PotionEffect(PotionEffectType.JUMP, duration * 20 * 2, 255),
        PotionEffect(PotionEffectType.NIGHT_VISION, 20 * 3, 1),
    )

    private val deathLineZ = girl.location.z + 3
    private val startLineZ = spawn[0].z - 11

    private var canKill = false
    private var walls = true

    override fun join(user: User) {
        user.player?.teleport(spawn.random())
    }

    override fun tick(time: Int): Int {
        if (time % 20 == 1) {
            Bonus.WEB.drop(bonus.random().clone().add((Math.random() - 0.5) * 12, 1.5, (Math.random() - 0.5) * 12))
            Bonus.SPEED.drop(bonus.random().clone().add((Math.random() - 0.5) * 12, 1.5, (Math.random() - 0.5) * 12))
        }
        return time
    }

    override fun registerHandlers(context: EventContext) {
        fork = context

        game.map.getLabels("circle1").forEach { Workers.CIRCLE.spawn(it) }

        val sounds = arrayOf(
            Music.KILL_PEAK,
            Music.KILL_FAST,
            Music.KILL_MIDDLE
        )

        var soundLock = false

        val period = 4.0

        MutableList(((duration / period - 1).toInt())) {
            (it + Math.random() / 2).toInt()
        }.forEach { time ->
            game.context.after(((20 * (PLAYER_PREPARE_DURATION + period + 1) + time * 20L * period).toLong())) {
                girl.rotate(game)
                if (!girl.forwardView && !soundLock) {
                    soundLock = true
                    game.after((period / 1.5 * 20).toLong()) { soundLock = false }
                    val random = sounds.random()
                    game.context.after(25) {
                        game.getUsers().forEach { random.play(it) }
                    }
                }
            }
        }

        fork.on<PlayerInteractEvent> {
            if (hasItem()) {
                if (item.getType() == Material.CLAY_BALL) {
                    player.itemInHand = null
                    player.removePotionEffect(PotionEffectType.SLOW)
                    fork.after(6 * 20) { player.addPotionEffects(effects) }
                } else if (item.getType() == Material.WEB){
                    val web = game.map.world.spawnFallingBlock(player.eyeLocation, Material.WEB, 0)
                    web.velocity = player.eyeLocation.direction
                    web.dropItem = false
                    player.itemInHand = null
                    isCancelled = true
                }
            }
        }
        fork.on<EntityDamageEvent> { isCancelled = true }
        fork.on<PlayerMoveEvent> {
            val user = app.getUser(player) ?: return@on

            if (to.z < deathLineZ && !user.spectator) {
                if (!user.roundWinner) {
                    AcceptRoundWin.accept(game, user)
                    user.timeOnGreenLight = (game.timer.time / 2.0).toInt() / 10.0
                    player.removePotionEffect(PotionEffectType.SLOW)
                } else if (to.z > deathLineZ - 2) {
                    player.velocity = Vector(0.0, 0.1, -0.3)
                }
                return@on
            } else if (to.z <= startLineZ && walls) { // start wall
                player.velocity = Vector(0.0, 0.1, 0.3)
            }

            if (player.isSprinting)
                player.isSprinting = false

            if (player.velocity.y > 0.5 && !player.isOnGround) {
                isCancelled = true
                cancel = true
            }

            if (!canKill || user.spectator || !girl.canView() || user.roundWinner || player.hasPotionEffect(
                    PotionEffectType.NIGHT_VISION))
                return@on
            if (from.blockX != to.blockX || from.blockZ != to.blockZ) {
                if (girl.forwardView) {
                    val random = turrets.values.random()
                    game.getUsers().forEach { Music.SHOOT.play(it) }
                    random.shoot(to)
                    backBullet(random)

                    AcceptLose.accept(game, user)
                }
            }
        }
    }

    override fun start() {
        canKill = true
        walls = false
        game.getUsers().forEach {
            Music.KILL_FAST.play(it)
            startPersonal(it)
        }
    }

    override fun startPersonal(user: User) {
        user.player?.addPotionEffects(effects)
        user.roundWinner = false
    }

    private fun backBullet(gun: Gun) {
        if (!gun.refill())
            fork.after(10) { backBullet(gun) }
    }

}