package net.nezerit6.animated_arsenal.screen;

import net.nezerit6.animated_arsenal.AnimatedGuns;
import net.nezerit6.animated_arsenal.item.BlueprintItem;
import net.nezerit6.animated_arsenal.network.ModNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class BlueprintScreen extends AbstractContainerScreen<BlueprintScreenHandler>
{
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(AnimatedGuns.MOD_ID, "textures/gui/inventory/blueprint.png");
    private int currentBlueprintIndex;

    public BlueprintScreen(BlueprintScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);

        this.imageWidth = 102;
        this.imageHeight = 96;
    }

    protected void setBlueprint(int index)
    {
        if (index < 0)
        {
            index = BlueprintItem.BLUEPRINT_ITEM_LIST.size() - 1;
        }
        else if (index >= BlueprintItem.BLUEPRINT_ITEM_LIST.size())
        {
            index = 0;
        }

        currentBlueprintIndex = index;
    }

    @Override
    protected void init()
    {
        super.init();
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;

        addRenderableWidget(Button.builder(Component.literal("❮"), b -> setBlueprint(currentBlueprintIndex - 1))
                .bounds(left + 30, top + 24, 10, 20).build());
        addRenderableWidget(Button.builder(Component.literal("❯"), b -> setBlueprint(currentBlueprintIndex + 1))
                .bounds(left + 62, top + 24, 10, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Set"), b ->
        {
            PacketDistributor.sendToServer(new ModNetwork.SelectBlueprint(this.currentBlueprintIndex));
            this.onClose();
        }).bounds(left + 35, top + 48, 32, 20).build());

        setBlueprint(0);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY)
    {
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY)
    {
        int i = this.leftPos;
        int j = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.renderItem(new ItemStack(BlueprintItem.BLUEPRINT_ITEM_LIST.get(this.currentBlueprintIndex)),
                43 + (this.width - this.imageWidth) / 2, 26 + (this.height - this.imageHeight) / 2);
    }
}