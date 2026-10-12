package net.mcreator.sscresearchanddevelopment.init;

import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

public final class SscResearchAndDevelopmentSoundSets {
    private SscResearchAndDevelopmentSoundSets() {}
    public static final List<DeferredHolder<SoundEvent, SoundEvent>> ARTEFACT_ACTIVATIONS = List.of(
            SscResearchAndDevelopmentSounds.ARTEFACT_ACTIVATE_1,
            SscResearchAndDevelopmentSounds.ARTEFACT_ACTIVATE_2,
            SscResearchAndDevelopmentSounds.ARTEFACT_ACTIVATE_3,
            SscResearchAndDevelopmentSounds.ARTEFACT_ACTIVATE_4,
            SscResearchAndDevelopmentSounds.ARTEFACT_ACTIVATE_5,
            SscResearchAndDevelopmentSounds.ARTEFACT_ACTIVATE_6,
            SscResearchAndDevelopmentSounds.ARTEFACT_ACTIVATE_7
    );
    public static final List<DeferredHolder<SoundEvent, SoundEvent>> KEYBOARD = List.of(
            SscResearchAndDevelopmentSounds.KEYBOARD_1,
            SscResearchAndDevelopmentSounds.KEYBOARD_2,
            SscResearchAndDevelopmentSounds.KEYBOARD_3,
            SscResearchAndDevelopmentSounds.KEYBOARD_4
    );
}
