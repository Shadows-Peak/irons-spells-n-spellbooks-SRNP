package io.redspace.ironsspellbooks.gui.overlays;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import io.redspace.ironsspellbooks.capabilities.spellbook.SpellBookData;
import io.redspace.ironsspellbooks.compat.Curios;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.player.ClientRenderCache;
import io.redspace.ironsspellbooks.api.util.Utils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.client.gui.overlay.ForgeGui;

import java.util.Objects;

public class SpellBarOverlay extends GuiComponent {
    public final static ResourceLocation TEXTURE = new ResourceLocation(IronsSpellbooks.MODID, "textures/gui/icons.png");
    static final int IMAGE_HEIGHT = 21;
    static final int IMAGE_WIDTH = 21;
    static final int HOTBAR_HALFWIDTH = 91;
    static final int boxSize = 20;
    static int screenHeight;
    static int screenWidth;

    private static ItemStack lastSpellBook = ItemStack.EMPTY;

    public static void render(ForgeGui gui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight) {
        Player player = Minecraft.getInstance().player;

        if (!Utils.isPlayerHoldingSpellBook(player))
            return;
        //System.out.println("SpellBarDisplay: Holding Spellbook");

        int centerX, centerY;
        centerX = screenWidth / 2 - Math.max(110, screenWidth / 4);
        centerY = screenHeight - Math.max(55, screenHeight / 8);

        //
        //  Render Spells
        //
        ItemStack spellBookStack = Utils.getPlayerSpellbookStack(player);
        var spellBookData = SpellBookData.getSpellBookData(spellBookStack);
        if (spellBookStack != lastSpellBook) {
            lastSpellBook = spellBookStack;
            ClientRenderCache.generateRelativeLocations(spellBookData, 20, 22);
        }

        var spells = spellBookData.getInscribedSpells();
        var locations = ClientRenderCache.relativeSpellBarSlotLocations;
        int approximateWidth = locations.size() / 3;
        //Move spellbar away from hotbar as it gets bigger
        centerX -= approximateWidth * 5;
        var spellSelection = ClientMagicData.getSyncedSpellData(player).getSpellSelection();
        int selectedSpellIndex = spellSelection.index;
        if (!spellSelection.equipmentSlot.equals(Curios.SPELLBOOK_SLOT)) {
            selectedSpellIndex = -1;
        }

        //Slot Border
        setTranslucentTexture(TEXTURE);
        for (Vec2 location : locations) {
            gui.blit(poseStack, centerX + (int) location.x, centerY + (int) location.y, 66, 84, 22, 22);
        }
        //Spell Icons
        for (int i = 0; i < locations.size(); i++) {
            if (spells[i] != null) {
                setOpaqueTexture(spells[i].getSpell().getSpellIconResource());
                gui.blit(poseStack, centerX + (int) locations.get(i).x + 3, centerY + (int) locations.get(i).y + 3, 0, 0, 16, 16, 16, 16);
            }
        }
        //Border + Cooldowns
        for (int i = 0; i < locations.size(); i++) {
            setTranslucentTexture(TEXTURE);
            if (i != selectedSpellIndex)
                gui.blit(poseStack, centerX + (int) locations.get(i).x, centerY + (int) locations.get(i).y, 22, 84, 22, 22);

            float f = spells[i] == null ? 0 : ClientMagicData.getCooldownPercent(spells[i].getSpell());
            if (f > 0) {
                int pixels = (int) (16 * f + 1f);
                gui.blit(poseStack, centerX + (int) locations.get(i).x + 3, centerY + (int) locations.get(i).y + 19 - pixels, 47, 87, 16, pixels);
            }
        }
        //Selected Outline
        for (int i = 0; i < locations.size(); i++) {
            setTranslucentTexture(TEXTURE);
            if (i == selectedSpellIndex)
                gui.blit(poseStack, centerX + (int) locations.get(i).x, centerY + (int) locations.get(i).y, 0, 84, 22, 22);
        }
    }

    private static void setOpaqueTexture(ResourceLocation texture) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, texture);
    }

    private static void setTranslucentTexture(ResourceLocation texture) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getRendertypeTranslucentShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, texture);
    }


}
