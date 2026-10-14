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
    public static final DeferredHolder<SoundEvent, SoundEvent> SERVER_FANS = registerSound("server_fans");
    public static final DeferredHolder<SoundEvent, SoundEvent> CIRCUIT_PRINTER = registerSound("circuit_printer");
    public static final DeferredHolder<SoundEvent, SoundEvent> TRAY_EJECT = registerSound("tray_eject");
    public static final DeferredHolder<SoundEvent, SoundEvent> TERMINAL_INSERT_DISC = registerSound("terminal_insert_disc");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCAN_LOOP = registerSound("scan_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> TELEPORT_ARRIVAL = registerSound("teleport_arrival");
    public static final DeferredHolder<SoundEvent, SoundEvent> RAD_PULSE_11 = registerSound("radpulse11");
    public static final DeferredHolder<SoundEvent, SoundEvent> BUTTON = registerSound("button");
    public static final DeferredHolder<SoundEvent, SoundEvent> HIGH_TECH_CONFIRM = registerSound("high_tech_confirm");
    private static DeferredHolder<SoundEvent, SoundEvent> registerSound(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MODID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }
}
