package me.func.util

import dev.implario.bukkit.event.EventContext
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Location
import org.bukkit.entity.EntityType

object Firework {

    fun generate(location: Location, context: EventContext, vararg colors: Color) {
        val firework = location.getWorld().spawnEntity(location, EntityType.FIREWORK) as org.bukkit.entity.Firework
        val meta = firework.fireworkMeta

        val effect = FireworkEffect.builder()
            .flicker(true)
            .trail(true)
        colors.forEach { color ->
            effect.with(FireworkEffect.Type.BALL)
            effect.withColor(color)
        }
        effect.build()

        meta.power = 0
        meta.addEffect(effect.build())

        firework.fireworkMeta = meta

        context.after(1) { firework.detonate() }
    }

}