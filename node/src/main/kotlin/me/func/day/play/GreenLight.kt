package me.func.day.play

import dev.implario.bukkit.event.EventContext
import dev.implario.bukkit.event.on
import me.func.AcceptLose
import me.func.AcceptRoundWin
import me.func.SquidGame
import me.func.app
import me.func.day.Day
import me.func.day.PLAYER_PREPARE_DURATION
import me.func.day.detail.Girl
import me.func.day.detail.Gun
import me.func.day.misc.Bonus
import me.func.day.misc.Workers
import me.func.mod.ModHelper
import me.func.user.User
import me.func.util.Music
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector

class GreenLight(private val game: SquidGame) : Day {

    override lateinit var fork: EventContext
    override val duration = 1 * 60 + 15
    override val description = arrayOf(
        "§fИспытание §b§l#1§b: Красный свет, зеленый свет",
        "   §7Стойте когда горит",
        "   §7красный и поспешите когда",
        "   §7виден зеленый, пересеките черту."
    )
    override val title = "§cКрасный §7свет, §aзеленый §7свет"

    private val spawn = game.map.getLabels("day1").map { it.toCenterLocation().add(0.0, 1.5, 0.0) }
    private val turrets = game.map.getLabels("gun").map { Gun(it) }.associateBy { it.uuid }
    private val girl = Girl(game.map.getLabel("girl"))
    private val effects = listOf(
        PotionEffect(PotionEffectType.SLOW, duration * 20 * 2, 1),
        PotionEffect(PotionEffectType.JUMP, duration * 20 * 2, 255),
    )

    private val deathLineZ = girl.location.z + 3
    private val startLineZ = spawn[0].z - 11

    private var canKill = false
    private var walls = true


    override fun join(user: User) {
        user.player.teleport(spawn.random())
    }

    override fun tick(time: Int) = time

    override fun registerHandlers(context: EventContext) {
        fork = context

        game.map.getLabels("boost").map { it.toCenterLocation().add(Math.random() * 8, 1.5, Math.random() * 8) }
            .forEach { Bonus.SPEED.drop(it) }
        game.map.getLabels("circle1").forEach { Workers.CIRCLE.spawn(it) }

        val sounds = arrayOf(
            Music.KILL_PEAK,
            Music.KILL_FAST,
            Music.KILL_MIDDLE
        )

        var soundLock = false

        val period = 3.5

        MutableList(((duration / period - 3).toInt())) {
            (it + Math.random() / 2).toInt()
        }.forEach { time ->
            game.context.after(((20 * (PLAYER_PREPARE_DURATION + period) + time * 20L * period).toLong())) {
                girl.rotate(game)
                if (!girl.forwardView && !soundLock) {
                    soundLock = true
                    game.after((period / 1.5 * 20).toLong()) { soundLock = false }
                    val random = sounds.random()
                    game.getUsers().forEach { random.play(it) }
                }
            }
        }

        fork.on<EntityDamageEvent> { isCancelled = true }
        fork.on<PlayerMoveEvent> {
            val user = app.getUser(player) ?: return@on

            if (to.z < deathLineZ && !user.spectator) {
                if (!user.roundWinner) {
                    AcceptRoundWin.accept(game, user)
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

            if (!canKill || user.spectator || !girl.canView() || user.roundWinner)
                return@on
            if (from.blockX != to.blockX || from.blockY != to.blockY || from.blockZ != to.blockZ) {
                val random = turrets.values.random()
                game.getUsers().forEach { Music.SHOOT.play(it) }
                random.shoot(to)
                backBullet(random)

                AcceptLose.accept(game, user)
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
        ModHelper.title(user, "§eОсторожно!")
        user.player.addPotionEffects(effects)
    }

    private fun backBullet(gun: Gun) {
        if (!gun.refill())
            fork.after(10) { backBullet(gun) }
    }

}