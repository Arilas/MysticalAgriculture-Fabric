package com.blakebr0.mysticalagriculture.registry;

import com.blakebr0.mysticalagriculture.MysticalAgriculture;
import com.blakebr0.mysticalagriculture.api.registry.IMobSoulTypeRegistry;
import com.blakebr0.mysticalagriculture.api.soul.MobSoulType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class MobSoulTypeRegistry implements IMobSoulTypeRegistry {
    private static final MobSoulTypeRegistry INSTANCE = new MobSoulTypeRegistry();

    private final Map<Identifier, MobSoulType> mobSoulTypes = new LinkedHashMap<>();
    private final Set<Identifier> usedEntityIds = new HashSet<>();
    private String currentSourceMod;
    private boolean finalized;

    MobSoulTypeRegistry() {
    }

    @Override
    public void register(MobSoulType mobSoulType) {
        this.requireMutable("register mob soul type " + mobSoulType.getId());
        if (this.currentSourceMod == null) {
            throw new IllegalStateException("Cannot register mob soul type outside a plug-in registration callback");
        }
        if (this.mobSoulTypes.containsKey(mobSoulType.getId())) {
            throw duplicate(mobSoulType.getId(), this.currentSourceMod);
        }

        var duplicateEntities = mobSoulType.getEntityIds().stream()
                .filter(this.usedEntityIds::contains)
                .collect(Collectors.toSet());
        if (!duplicateEntities.isEmpty()) {
            throw new IllegalStateException(
                    "Duplicate mob soul entity ids %s contributed by mod %s".formatted(duplicateEntities, this.currentSourceMod)
            );
        }

        this.mobSoulTypes.put(mobSoulType.getId(), mobSoulType);
        this.usedEntityIds.addAll(mobSoulType.getEntityIds());
    }

    @Override
    public List<MobSoulType> getMobSoulTypes() {
        return List.copyOf(this.mobSoulTypes.values());
    }

    @Override
    public MobSoulType getMobSoulTypeById(Identifier id) {
        return this.mobSoulTypes.get(id);
    }

    @Override
    public MobSoulType getMobSoulTypeByEntity(LivingEntity entity) {
        return this.mobSoulTypes.values().stream().filter(t -> t.isEntityApplicable(entity)).findFirst().orElse(null);
    }

    @Override
    public Set<Identifier> getUsedEntityIds() {
        return Collections.unmodifiableSet(this.usedEntityIds);
    }

    @Override
    public boolean addEntityTo(MobSoulType type, Identifier entity) {
        this.requireMutable("add a mob soul entity");
        if (this.usedEntityIds.add(entity)) {
            type.getEntityIds().add(entity);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeEntityFrom(MobSoulType type, Identifier entity) {
        this.requireMutable("remove a mob soul entity");
        if (type.getEntityIds().remove(entity)) {
            this.usedEntityIds.remove(entity);
            return true;
        }
        return false;
    }

    public static MobSoulTypeRegistry getInstance() {
        return INSTANCE;
    }

    public void onCommonSetup() {
        MysticalAgriculture.LOGGER.info("Loaded {} mob soul types", this.mobSoulTypes.size());
    }

    void beginRegistration(String sourceMod) {
        this.requireMutable("begin registration");
        this.currentSourceMod = sourceMod;
    }

    void endRegistration() {
        this.currentSourceMod = null;
    }

    void finalizeRegistration() {
        this.requireMutable("finalize registration");
        this.finalized = true;
        this.onCommonSetup();
    }

    private void requireMutable(String action) {
        if (this.finalized) {
            throw new IllegalStateException("Cannot %s after the mob soul registry was finalized".formatted(action));
        }
    }

    private static IllegalStateException duplicate(Identifier id, String sourceMod) {
        return new IllegalStateException("Duplicate mob soul id %s contributed by mod %s".formatted(id, sourceMod));
    }
}
