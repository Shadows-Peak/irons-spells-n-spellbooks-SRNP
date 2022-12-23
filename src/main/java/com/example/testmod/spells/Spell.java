package com.example.testmod.spells;

import com.example.testmod.StaticEventHandler;
import com.example.testmod.TestMod;
import com.example.testmod.capabilities.mana.client.ClientManaData;
import com.example.testmod.capabilities.mana.network.PacketCastSpell;
import com.example.testmod.setup.Messages;
import io.netty.util.concurrent.SucceededFuture;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;

public abstract class Spell {
    protected int level;
    protected int baseManaCost;
    protected int manaCostPerLevel;
    protected int baseSpellPower;
    protected int spellPowerPerLevel;
    protected boolean continuous;
    protected int maxCooldown;
    protected int currentCooldown;

    public int getManaCost() {
        return baseManaCost + manaCostPerLevel * (level - 1);
    }

    public int getSpellPower() {
        return baseSpellPower + spellPowerPerLevel * (level - 1);
    }

    //returns true/false for success/failure to cast
    public boolean attemptCast(ItemStack stack, Level world, Player player) {
        //fill with all casting criteria
        boolean canCast = !isOnCooldown() &&
                ClientManaData.getPlayerMana() >= getManaCost();

        if (canCast) {
            this.onCast(stack, world, player);
            if(!world.isClientSide()){
                startCooldown(player);
                Messages.sendToServer(new PacketCastSpell(getManaCost()));
            }
            return true;
        } else {
            return false;
        }
    }
    public void tick(){
        if(isOnCooldown())
            currentCooldown--;
    }
    public abstract void onCast(ItemStack stack, Level world, Player player);

    public boolean isOnCooldown(){
        return currentCooldown > 0;
    }

    public int getModifiedCastCooldown(Player caster){
        float attributeCooldownReductionPlaceholder = 1;
        return (int) (maxCooldown/attributeCooldownReductionPlaceholder);
    }
    public float getPercentCooldown(){
        return Mth.clamp(currentCooldown/(float)maxCooldown,0,1);
    }
    public void startCooldown(@Nullable Player caster){
        if(caster == null)
            currentCooldown = maxCooldown;
        else
            currentCooldown = getModifiedCastCooldown(caster);
    }
}
