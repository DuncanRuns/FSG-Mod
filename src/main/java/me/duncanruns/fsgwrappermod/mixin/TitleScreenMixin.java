package me.duncanruns.fsgwrappermod.mixin;

import me.duncanruns.fsgwrappermod.screen.ConfigScreen;
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
    @Unique private static final Random RANDOM = new Random();
    @Shadow @Final private boolean isMinceraft;
    @Unique
    private final int seedOffset = RANDOM.nextInt(2); // If you found this, yes, it's making the seed render one pixel off at random.

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void init(CallbackInfo info) {
        this.addButton(new ButtonWidget(this.width / 2 - 124, this.height / 4 + 48 + 24, 20, 20, new LiteralText(""), (b) -> {
            client.openScreen(new ConfigScreen());
        }));
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void wheatSeedsOverlay(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        this.client.getTextureManager().bindTexture(BUTTON_IMAGE);
        drawTexture(matrices, this.width / 2 - 124 + 1 + (isMinceraft ? RANDOM.nextInt(2) : seedOffset), this.height / 4 + 48 + 2 + 24, 0.0F, 0.0F, 16, 16, 16, 16);
    }

}
