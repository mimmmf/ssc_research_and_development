package net.mcreator.sscresearchanddevelopment.init;

import net.mcreator.sscresearchanddevelopment.SscResearchAndDevelopmentMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SscResearchAndDevelopmentSounds {
    public static final String MODID = SscResearchAndDevelopmentMod.MODID;

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, MODID);
    public static final DeferredHolder<SoundEvent, SoundEvent> ARTEFACT_ACTIVATE_1 = registerSound("artefact_activate_1");
    public static final DeferredHolder<SoundEvent, SoundEvent> ARTEFACT_ACTIVATE_2 = registerSound("artefact_activate_2");
    public static final DeferredHolder<SoundEvent, SoundEvent> ARTEFACT_ACTIVATE_3 = registerSound("artefact_activate_3");
    public static final DeferredHolder<SoundEvent, SoundEvent> ARTEFACT_ACTIVATE_4 = registerSound("artefact_activate_4");
    public static final DeferredHolder<SoundEvent, SoundEvent> ARTEFACT_ACTIVATE_5 = registerSound("artefact_activate_5");
    public static final DeferredHolder<SoundEvent, SoundEvent> ARTEFACT_ACTIVATE_6 = registerSound("artefact_activate_6");
    public static final DeferredHolder<SoundEvent, SoundEvent> ARTEFACT_ACTIVATE_7 = registerSound("artefact_activate_7");
    public static final DeferredHolder<SoundEvent, SoundEvent> KEYBOARD_1 = registerSound("keyboard1");
    public static final DeferredHolder<SoundEvent, SoundEvent> KEYBOARD_2 = registerSound("keyboard2");
    public static final DeferredHolder<SoundEvent, SoundEvent> KEYBOARD_3 = registerSound("keyboard3");
    public static final DeferredHolder<SoundEvent, SoundEvent> KEYBOARD_4 = registerSound("keyboard4");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCAN_FINISH = registerSound("scan_finish");

    private static DeferredHolder<SoundEvent, SoundEvent> registerSound(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MODID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }
}
