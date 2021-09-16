package com.fuzs.easymagic.client.element;

import com.fuzs.easymagic.element.GearSlotsEverywhereElement;
import com.fuzs.easymagic.network.client.message.C2SMoveGearSlotsMessage;
import com.fuzs.puzzleslib_em.PuzzlesLib;
import com.fuzs.puzzleslib_em.element.extension.ElementExtension;
import com.fuzs.puzzleslib_em.element.side.IClientElement;
import net.minecraftforge.fml.LogicalSide;

public class GearSlotsEverywhereExtension extends ElementExtension<GearSlotsEverywhereElement> implements IClientElement {

    public GearSlotsEverywhereExtension(GearSlotsEverywhereElement parent) {

        super(parent);
    }

    @Override
    public void initClient() {

        PuzzlesLib.getNetworkHandler().registerMessage(C2SMoveGearSlotsMessage::new, LogicalSide.SERVER);
    }

}
