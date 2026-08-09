package me.duncanruns.fsgmod.mixin;

import me.duncanruns.fsgmod.screen.ConfigScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    @Unique
    private static final Identifier BUTTON_IMAGE = new Identifier("textures/item/wheat_seeds.png");
    @Unique
    private static final Random RANDOM = new Random();
    @Shadow
    @Final
    private boolean isMinceraft;
    @Unique
    private final int seedOffset = RANDOM.nextInt(2); // If you found this, yes, it's making the seed render one pixel off at random.

    @Unique
    private ButtonWidget fsgConfigButton;

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void init(CallbackInfo info) {
        // Save the button instance to the variable instead of just adding it
        this.fsgConfigButton = this.addButton(new ButtonWidget(this.width / 2 - 124, this.height / 4 + 48 + 24, 20, 20, new LiteralText(""), (b) -> {
            assert client != null;
            client.openScreen(new ConfigScreen());
        }));
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void wheatSeedsOverlay(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.fsgConfigButton == null) return; // Safety check to prevent crashes

        assert this.client != null;
        this.client.getTextureManager().bindTexture(BUTTON_IMAGE);
        
        // Dynamically grab the button's X and Y coordinates so the texture tracks the box
        int iconX = this.fsgConfigButton.x + 1 + (isMinceraft ? RANDOM.nextInt(2) : seedOffset);
        int iconY = this.fsgConfigButton.y + 2;
        
        drawTexture(matrices, iconX, iconY, 0.0F, 0.0F, 16, 16, 16, 16);
    }

}