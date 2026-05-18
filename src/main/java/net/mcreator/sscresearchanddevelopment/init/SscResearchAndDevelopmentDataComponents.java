package net.mcreator.sscresearchanddevelopment.init;

import com.mojang.serialization.Codec;
import net.mcreator.sscresearchanddevelopment.component.LinkedBlockComponent;
import net.mcreator.sscresearchanddevelopment.component.ArtifactProperties;
import net.mcreator.sscresearchanddevelopment.component.MultitoolComponent;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SscResearchAndDevelopmentDataComponents {
	public static final DeferredRegister<DataComponentType<?>> COMPONENTS = 
        DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, "ssc_research_and_development");

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ArtifactProperties>> ARTIFACT_DATA = 
        COMPONENTS.register("artifact_data", () -> DataComponentType.<ArtifactProperties>builder()
            .persistent(ArtifactProperties.CODEC)
            .networkSynchronized(ArtifactProperties.STREAM_CODEC)
            .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<GlobalPos>>> LINKED_BLOCKS =
            COMPONENTS.register("linked_blocks", () -> DataComponentType.<List<GlobalPos>>builder()
                    .persistent(Codec.list(GlobalPos.CODEC))
                    .networkSynchronized(
                            ByteBufCodecs.collection(
                                    size -> new ArrayList<>(size),
                                    GlobalPos.STREAM_CODEC,
                                    256
                            )
                    )
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MultitoolComponent>> MULTITOOL_DATA =
            COMPONENTS.register("multitool_data", () -> DataComponentType.<MultitoolComponent>builder()
                    .persistent(MultitoolComponent.CODEC)
                    .networkSynchronized(MultitoolComponent.STREAM_CODEC)
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<LinkedBlockComponent>> LINKED_BLOCK =
            COMPONENTS.register("linked_block", () -> DataComponentType.<LinkedBlockComponent>builder()
                    .persistent(LinkedBlockComponent.CODEC)
                    .networkSynchronized(LinkedBlockComponent.STREAM_CODEC)
                    .build());
    public static final Supplier<DataComponentType<Integer>> SCORE = COMPONENTS.register("score",
            () -> DataComponentType.<Integer>builder().persistent(ExtraCodecs.NON_NEGATIVE_INT).build());
}