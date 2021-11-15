package me.func.day.misc

import me.func.mod.ModHelper
import org.bukkit.Location
import java.util.*

enum class Workers(private val title: String, private val uuid: UUID) {
    CIRCLE("Круглый", UUID.fromString("7427c507-373b-11eb-acca-1cb72caa35fd")),
    TRIANGLE("Треугольный", UUID.fromString("9a109585-e5e5-11ea-acca-1cb72caa35fd")),
    SQUARE("Квадратный", UUID.fromString("3089411e-2c69-11e8-b5ea-1cb72caa35fd")),;

    fun spawn(location: Location) {
        spawn(location, null)
    }

    fun spawn(location: Location, name: String?) {
        ModHelper.npc(location, uuid, name, null)
    }
}