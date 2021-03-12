package com.fuzs.easymagic.mixin;

import com.fuzs.easymagic.EasyMagic;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;

@SuppressWarnings("unused")
public class MixinConnector implements IMixinConnector {

    @Override
    public void connect() {

        Mixins.addConfiguration("META-INF/" + EasyMagic.MODID + ".mixins.json");
    }

}
