package fuzs.easymagic.util;

import net.minecraft.world.entity.player.Player;

/**
 * when retrieving total experience points (not enchantment levels) Player#totalExperience doesn't work as it isn't always updated when levels are added
 */
public class PlayerExperienceHelper {

    public static int getTotalExperience(Player player) {
        return getExperienceFromLevels(player.experienceLevel) + (int) (player.getXpNeededForNextLevel() * player.experienceProgress);
    }

    public static int getExperienceFromLevels(int experienceLevel) {
        // thanks Minecraft Wiki!
        if (experienceLevel < 17) return experienceLevel * experienceLevel + 6 * experienceLevel;
        if (experienceLevel < 32) return (int) (2.5F * experienceLevel * experienceLevel - 40.5F * experienceLevel + 360);
        return (int) (4.5F * experienceLevel * experienceLevel - 162.5F * experienceLevel + 2220);
    }
}
