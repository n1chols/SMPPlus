package net.tob4n.smpplus.structures

import org.bukkit.NamespacedKey
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask

abstract class Feature : Listener {
    companion object {
        lateinit var plugin: JavaPlugin
    }

    var isEnabled = false
        private set

    private val tasks = mutableListOf<BukkitTask>()

    fun enable() {
        if (!isEnabled) {
            isEnabled = true
            plugin.server.pluginManager.registerEvents(this, plugin)
            onEnable()
        }
    }

    fun disable() {
        if (isEnabled) {
            isEnabled = false
            HandlerList.unregisterAll(this)
            tasks.forEach { it.cancel() }
            tasks.clear()
            onDisable()
        }
    }

    protected fun runTask(delay: Long = 0L, period: Long = 0L, async: Boolean = false, task: Runnable): BukkitTask {
        val bukkitTask = when {
            async && period > 0 -> plugin.server.scheduler.runTaskTimerAsynchronously(plugin, task, delay, period)
            async -> plugin.server.scheduler.runTaskLaterAsynchronously(plugin, task, delay)
            period > 0 -> plugin.server.scheduler.runTaskTimer(plugin, task, delay, period)
            else -> plugin.server.scheduler.runTaskLater(plugin, task, delay)
        }
        tasks.add(bukkitTask)
        return object : BukkitTask by bukkitTask {
            override fun cancel() {
                bukkitTask.cancel()
                tasks.remove(bukkitTask)
            }
        }
    }

    protected open fun onEnable() {}

    protected open fun onDisable() {}
}
