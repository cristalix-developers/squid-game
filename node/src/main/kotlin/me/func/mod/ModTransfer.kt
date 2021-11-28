package me.func.mod

import io.netty.buffer.Unpooled
import me.func.user.User
import net.minecraft.server.v1_12_R1.PacketDataSerializer
import net.minecraft.server.v1_12_R1.PacketPlayOutCustomPayload
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import ru.cristalix.core.GlobalSerializers

class ModTransfer {
    private val serializer = PacketDataSerializer(Unpooled.buffer())

    fun json(`object`: Any?): ModTransfer {
        return string(GlobalSerializers.toJson(`object`))
    }

    fun string(string: String?): ModTransfer {
        serializer.writeString(string)
        return this
    }

    fun item(item: net.minecraft.server.v1_12_R1.ItemStack): ModTransfer {
        serializer.writeItem(item)
        return this
    }

    fun item(item: ItemStack): ModTransfer {
        serializer.writeItem(CraftItemStack.asNMSCopy(item))
        return this
    }

    fun integer(integer: Int): ModTransfer {
        serializer.writeInt(integer)
        return this
    }

    fun double(double: Double): ModTransfer {
        serializer.writeDouble(double)
        return this
    }

    fun boolean(boolean: Boolean): ModTransfer {
        serializer.writeBoolean(boolean)
        return this
    }

    fun send(channel: String?, user: User?) {
        user?.sendPacket(PacketPlayOutCustomPayload(channel, serializer))
    }
}