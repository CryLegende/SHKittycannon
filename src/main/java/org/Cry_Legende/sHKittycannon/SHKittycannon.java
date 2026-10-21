package org.Cry_Legende.sHKittycannon;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Cat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SHKittycannon extends JavaPlugin implements TabExecutor, Listener {

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.getCommand("kittycannon").setExecutor(this);
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("shkittycannon.use")) {
                launchKitty(player);
            } else {
                player.sendMessage("Vous n'avez pas la permission d'utiliser cette commande.");
            }
            return true;
        }
        sender.sendMessage("Seul un joueur peut exécuter cette commande.");
        return false;
    }

    private void launchKitty(Player player) {
        // Positionner le chat un bloc au-dessus et à une distance légèrement réduite
        Location spawnLocation = player.getLocation().add(player.getLocation().getDirection().multiply(1.2)).add(0, 1, 0);
        Cat cat = (Cat) player.getWorld().spawnEntity(spawnLocation, EntityType.CAT);
        cat.setOwner(player);
        cat.setSitting(true);

        // Jouer le son de miaulement lorsque le chat apparaît avec un volume réduit
        cat.getWorld().playSound(cat.getLocation(), Sound.ENTITY_CAT_AMBIENT, 0.3F, 1.0F);

        // Récupérer la chance de bébé chat depuis la config
        int babyCatChance = getConfig().getInt("baby-cat-chance", 10);

        // Générer un nombre aléatoire pour décider si le chat sera un bébé
        if (Math.random() * 100 < babyCatChance) {
            cat.setBaby();
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (cat.isValid()) {
                    Location catLocation = cat.getLocation();
                    // Utiliser une particule ressemblant à des croissants de lune
                    cat.getWorld().spawnParticle(Particle.POOF, catLocation, 1, 0, 0, 0, 0);
                    cat.getWorld().playSound(catLocation, Sound.ENTITY_GENERIC_EXPLODE, 0.1F, 1.6F);
                    cat.remove();
                }
            }
        }.runTaskLater(this, 40L); // 40L = 2 seconds (20 ticks per second)

        // Réduire la vélocité du chat pour le lancer moins fort
        cat.setVelocity(player.getLocation().getDirection().multiply(1.2));
    }

    @EventHandler
    public void onEntityInteract(EntityInteractEvent event) {
        if (event.getEntity() instanceof Cat) {
            Cat cat = (Cat) event.getEntity();
            Location loc = cat.getLocation();
            Material material = event.getBlock().getType();

            // Vérifier si le matériau est une plaque de pression ou un tripwire hook
            if (material.name().contains("PRESSURE_PLATE") || material.name().contains("TRIPWIRE")) {
                event.setCancelled(true);
            }
        }
    }
}
