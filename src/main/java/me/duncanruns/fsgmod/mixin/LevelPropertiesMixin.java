package me.duncanruns.fsgmod.mixin;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import me.duncanruns.fsgmod.SeedManager;
import me.duncanruns.fsgmod.duck.TokenHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.registry.RegistryTracker;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.SaveVersionInfo;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(LevelProperties.class)
public abstract class LevelPropertiesMixin implements TokenHolder {
    @Unique
    private String token = null;

    @Inject(method = "method_29029", at = @At("RETURN"))
    private static void readLevelDat(Dynamic<Tag> dynamic, DataFixer dataFixer, int i, @Nullable CompoundTag compoundTag, LevelInfo levelInfo, SaveVersionInfo saveVersionInfo, GeneratorOptions generatorOptions, Lifecycle lifecycle, CallbackInfoReturnable<LevelProperties> cir) {
        ((TokenHolder) cir.getReturnValue()).fsgmod$setToken(Optional.ofNullable(dynamic.getElement("FSGToken", null)).map(Tag::asString).orElse(null));
    }

    @Inject(method = "updateProperties", at = @At("TAIL"))
    private void addLeveldat(RegistryTracker registryTracker, CompoundTag compoundTag, CompoundTag compoundTag2, CallbackInfo ci) {
        if (token != null) compoundTag.putString("FSGToken", token);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/LevelInfo;Lnet/minecraft/world/gen/GeneratorOptions;Lcom/mojang/serialization/Lifecycle;)V", at = @At("TAIL"))
    private void onNewLevelProperties(LevelInfo levelInfo, GeneratorOptions generatorOptions, Lifecycle lifecycle, CallbackInfo ci) {
        SeedManager.getResultForSeed(generatorOptions.getSeed()).ifPresent(result -> token = result.token);
    }

    @Override
    public String fsgmod$getToken() {
        return token;
    }

    @Override
    public void fsgmod$setToken(String token) {
        this.token = token;
    }
}
