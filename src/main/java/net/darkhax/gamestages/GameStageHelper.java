package net.darkhax.gamestages;

import net.darkhax.gamestages.data.GameStageSaveHandler;
import net.darkhax.gamestages.data.IStageData;
import net.darkhax.gamestages.event.GameStageEvent;
import net.darkhax.gamestages.packet.MessageStages;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.util.thread.EffectiveSide;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class GameStageHelper {
    
    /**
     * Predicate for matching the valid characters of a stage name.
     */
    private static final Predicate<String> STAGE_PATTERN = Pattern.compile("^[a-z0-9_:]*$").asPredicate();
    
    /**
     * Checks if a string is valid as a stage name. Compares against {@link #STAGE_PATTERN}.
     * 
     * @param stageName The potential name.
     * @return Whether or not the name is valid as a stage name.
     */
    public static boolean isValidStageName (String stageName) {

        return STAGE_PATTERN.test(stageName);
    }
    

    
    /**
     * Gets an immutable set of all the stages defined in the known stages json file.
     * 
     * @return An immutable set of all known stages.
     */
    public static Set<String> getKnownStages () {
        
        return GameStageSaveHandler.getKnownStages();
    }
    
    /**
     * Checks if a stage has been defined in the known stages file.
     * 
     * @param stage The stage name to search for.
     * @return Whether or not the stage name exists in the known stages.
     */
    public static boolean isStageKnown (String stage) {
        
        return GameStageSaveHandler.isStageKnown(stage);
    }
    
    /**
     * Checks if a player has a stage.
     * 
     * @param player The player to check.
     * @param stage The stage to look for.
     * @return Whether or not they have the stage.
     */
    public static boolean hasStage (Player player, String stage) {
        
        return hasStage(player, getPlayerData(player), stage);
    }
    
    /**
     * Checks if a player has a stage.
     * 
     * @param player The player to check.
     * @param data The player's stage data.
     * @param stage The stage to look for.
     * @return Whether or not they have the stage.
     */
    public static boolean hasStage (Player player, @Nullable IStageData data, String stage) {
        
        if (data != null) {
            
            final GameStageEvent.Check event = new GameStageEvent.Check(player, stage, data.hasStage(stage));
            MinecraftForge.EVENT_BUS.post(event);
            return event.hasStage();
        }
        
        return false;
    }
    
    /**
     * Checks if the player has at least one of many possible stages.
     * 
     * @param player The player to check.
     * @param stages The stages to look for.
     * @return Whether or not the player had at least one of the stages.
     */
    public static boolean hasAnyOf (Player player, String... stages) {
        
        return hasAnyOf(player, getPlayerData(player), stages);
    }
    
    /**
     * Checks if the player has at least one of many possible stages.
     * 
     * @param player The player to check.
     * @param stages The stages to look for.
     * @return Whether or not the player had at least one of the stages.
     */
    public static boolean hasAnyOf (Player player, Collection<String> stages) {
        
        return hasAnyOf(player, getPlayerData(player), stages);
    }
    
    /**
     * Checks if the player has at least one of many possible stages.
     * 
     * @param player The player to check.
     * @param data The player's stage data.
     * @param stages The stages to look for.
     * @return Whether or not the player had at least one of the stages.
     */
    public static boolean hasAnyOf (Player player, @Nullable IStageData data, Collection<String> stages) {
        
        return stages.stream().anyMatch(stage -> hasStage(player, data, stage));
    }
    
    /**
     * Checks if the player has at least one of many possible stages.
     * 
     * @param player The player to check.
     * @param data The player's stage data.
     * @param stages The stages to look for.
     * @return Whether or not the player had at least one of the stages.
     */
    public static boolean hasAnyOf (Player player, @Nullable IStageData data, String... stages) {
        
        return Arrays.stream(stages).anyMatch(stage -> hasStage(player, data, stage));
    }
    
    /**
     * Checks if the player has all of the stages.
     * 
     * @param player The player to check.
     * @param stages The stages to look for.
     * @return Whether or not the player had all the stages.
     */
    public static boolean hasAllOf (Player player, String... stages) {
        
        return hasAllOf(player, getPlayerData(player), stages);
    }
    
    /**
     * Checks if the player has all of the stages.
     * 
     * @param player The player to check.
     * @param stages The stages to look for.
     * @return Whether or not the player had all the stages.
     */
    public static boolean hasAllOf (Player player, Collection<String> stages) {
        
        return hasAllOf(player, getPlayerData(player), stages);
    }
    
    /**
     * Checks if the player has all of the stages.
     * 
     * @param player The player to check.
     * @param data The player's stage data.
     * @param stages The stages to look for.
     * @return Whether or not the player had all the stages.
     */
    public static boolean hasAllOf (Player player, @Nullable IStageData data, Collection<String> stages) {
        
        return stages.stream().allMatch(stage -> hasStage(player, data, stage));
    }
    
    /**
     * Checks if the player has all of the stages.
     * 
     * @param player The player to check.
     * @param data The player's stage data.
     * @param stages The stages to look for.
     * @return Whether or not the player had all the stages.
     */
    public static boolean hasAllOf (Player player, @Nullable IStageData data, String... stages) {
        
        return Arrays.stream(stages).allMatch(stage -> hasStage(player, data, stage));
    }
    
    /**
     * Attempts to give a player a stage. Events may cancel this.
     * 
     * @param player The player to give the stage.
     * @param stage The stage to give.
     */
    public static void addStage (ServerPlayer player, String stage) {
        
        if (!MinecraftForge.EVENT_BUS.post(new GameStageEvent.Add(player, stage))) {
            
            final IStageData data = getPlayerData(player);
            
            if (data != null) {
                
                data.addStage(stage);
                syncPlayer(player);
                MinecraftForge.EVENT_BUS.post(new GameStageEvent.Added(player, stage));
            }
        }
    }
    
    /**
     * Attempts to remove a stage from a player. Events may cancel this.
     * 
     * @param player The player to remove the stage from.
     * @param stage The stage to remove.
     */
    public static void removeStage (ServerPlayer player, String stage) {
        
        if (!MinecraftForge.EVENT_BUS.post(new GameStageEvent.Remove(player, stage))) {
            
            final IStageData data = getPlayerData(player);
            
            if (data != null) {
                
                data.removeStage(stage);
                syncPlayer(player);
                MinecraftForge.EVENT_BUS.post(new GameStageEvent.Removed(player, stage));
            }
        }
    }
    
    /**
     * Removes all stages from a player.
     * 
     * @param player The player to clear the stages of.
     * @return The amount of stages that were removed.
     */
    public static int clearStages (ServerPlayer player) {
        
        final IStageData stageInfo = GameStageHelper.getPlayerData(player);
        
        if (stageInfo != null) {
            
            final int stageCount = stageInfo.getStages().size();
            stageInfo.clear();
            syncPlayer(player);
            MinecraftForge.EVENT_BUS.post(new GameStageEvent.Cleared(player, stageInfo));
            return stageCount;
        }
        
        return 0;
    }
    
    /**
     * Attempts to resolve the stage data for a player. If it is a real server player it will
     * lookup their data using UUID. If it's a FakePlayer it will check the fake player data
     * file. If it's a client player it will use the client's synced data cache.
     * 
     * @param player The player to resolve.
     * @return The stage data that was found. Will be null if nothing could be found.
     */
    @Nullable
    public static IStageData getPlayerData (Player player) {
        
        if (player != null) {
            
            if (player instanceof ServerPlayer) {
                
                if (player instanceof FakePlayer) {
                    
                    return GameStageSaveHandler.getFakeData(player.getName().getString());
                }
                
                return GameStageSaveHandler.getPlayerData(player.getUUID());
            }
            
            else if (EffectiveSide.get().isClient()) {
                
                return GameStageSaveHandler.getClientData();
            }
        }
        
        return null;
    }
    
    /**
     * Syncs a player's stage data from the server to the client.
     * 
     * @param player The player to sync.
     */
    public static void syncPlayer (ServerPlayer player) {
        
        final IStageData info = GameStageHelper.getPlayerData(player);
        
        if (info != null) {
            
            GameStages.LOG.debug("Syncing {} stages for {}.", info.getStages().size(), player.getName());
            GameStages.NETWORK.syncPlayerStages(player, new MessageStages(info.getStages()));
        }
    }
}