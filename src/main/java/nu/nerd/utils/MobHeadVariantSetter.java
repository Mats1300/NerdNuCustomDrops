package nu.nerd.utils;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.DyeColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.*;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class to apply a "variant" string to entities.
 * Handles color, type, breed, behavior, and special identifiers (like jeb_ sheep).
 */
public class MobHeadVariantSetter {

    /** Apply a variant string to the entity (color/type/etc) */
    public static void applyVariant(Entity entity, String variantKey) {
        if (entity == null || variantKey == null) return;

        // Use lower-case keys for NamespacedKey lookup
        String key = variantKey.toLowerCase(Locale.ROOT);

        switch (entity.getType()) {
            case AXOLOTL -> {
                if (entity instanceof Axolotl axolotl) {
                    try { axolotl.setVariant(Axolotl.Variant.valueOf(variantKey.toUpperCase(Locale.ROOT)));
                    } catch (IllegalArgumentException ignored) {}
                }
            }
            case SHEEP -> {
                if (entity instanceof Sheep sheep) {
                    if ("jeb_".equalsIgnoreCase(variantKey)) sheep.customName(Component.text("jeb_"));
                    else try { sheep.setColor(DyeColor.valueOf(variantKey.toUpperCase(Locale.ROOT)));
                    } catch (IllegalArgumentException ignored) {}
                }
            }
            case CAT -> {
                if (entity instanceof Cat cat) {
                    try {
                        Registry<Cat.@NotNull Type> catRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.CAT_VARIANT);
                        NamespacedKey k = NamespacedKey.minecraft(variantKey.toLowerCase(Locale.ROOT));
                        Cat.Type type = catRegistry.get(k);
                        if (type != null) {
                            cat.setCatType(type);
                        }
                    } catch (Exception e) {
                        // handle missing or invalid variant
                    }
                }
            }
            case HORSE -> {
                if (entity instanceof Horse horse) {
                    try {
                        horse.setColor(Horse.Color.valueOf(variantKey.toUpperCase(Locale.ROOT)));
                    } catch (IllegalArgumentException ignored) {}
                }
            }
            case LLAMA, TRADER_LLAMA -> {
                if (entity instanceof Llama llama) {
                    try {
                        llama.setColor(Llama.Color.valueOf(variantKey.toUpperCase(Locale.ROOT)));
                    } catch (IllegalArgumentException ignored) {}
                }
            }
            case FOX -> {
                if (entity instanceof Fox fox) {
                    try {
                        fox.setFoxType(Fox.Type.valueOf(variantKey.toUpperCase(Locale.ROOT)));
                    } catch (IllegalArgumentException ignored) {}
                }
            }
            case WOLF -> {
                if (entity instanceof Wolf wolf) {
                    try {
                        Registry<Wolf.@NotNull Variant> wolfRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.WOLF_VARIANT);
                        NamespacedKey k = NamespacedKey.minecraft(variantKey.toLowerCase(Locale.ROOT));
                        Wolf.Variant var = wolfRegistry.get(k);
                        if (var != null) {
                            wolf.setVariant(var);
                        }
                    } catch (Exception ignored) {}
                }
            }
            case BEE -> {
                if (entity instanceof Bee bee) {
                    bee.setAnger("angry".equalsIgnoreCase(variantKey) ? 100 : 0);
                }
            }
            case GHAST -> {
                if (entity instanceof Ghast ghast) {
                    // This just switches the charging state
                    ghast.setCharging("shooting".equalsIgnoreCase(variantKey));
                }
            }
            case PANDA -> {
                if (entity instanceof Panda panda) {
                    try {
                        panda.setMainGene(Panda.Gene.valueOf(variantKey.toUpperCase(Locale.ROOT)));
                    } catch (IllegalArgumentException ignored) {}
                }
            }
            case RABBIT -> {
                if (entity instanceof Rabbit rabbit) {
                    try {
                        rabbit.setRabbitType(Rabbit.Type.valueOf(variantKey.toUpperCase(Locale.ROOT)));
                    } catch (IllegalArgumentException ignored) {}
                }
            }
            case SNOW_GOLEM -> {
                // Snow Golem variants ("derp") have no setter in API
            }
            case STRIDER -> {
                if (entity instanceof Strider strider) {
                    strider.setShivering("shivering".equalsIgnoreCase(variantKey));
                }
            }
            case VILLAGER -> {
                if (entity instanceof Villager villager) {
                    // Lookup villager profession from registry
                    Villager.Profession prof = Registry.VILLAGER_PROFESSION.get(NamespacedKey.minecraft(key));
                    if (prof != null) {
                        villager.setProfession(prof);
                    }
                }
            }
            case ZOMBIE_VILLAGER -> {
                if (entity instanceof ZombieVillager zombieVillager) {
                    // Lookup zombie villager profession (same registry as Villager)
                    Villager.Profession prof = Registry.VILLAGER_PROFESSION.get(NamespacedKey.minecraft(key));
                    if (prof != null) {
                        zombieVillager.setVillagerProfession(prof);
                    }
                }
            }
            case FROG -> {
                if (entity instanceof Frog frog) {
                    try {
                        Registry<Frog.@NotNull Variant> frogRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.FROG_VARIANT);
                        NamespacedKey k = NamespacedKey.minecraft(variantKey.toLowerCase(Locale.ROOT));
                        Frog.Variant var = frogRegistry.get(k);
                        if (var != null) {
                            frog.setVariant(var);
                        }
                    } catch (Exception ignored) {}
                }
            }
            case MOOSHROOM -> {
                if (entity instanceof MushroomCow mc) {
                    try {
                        mc.setVariant(MushroomCow.Variant.valueOf(variantKey.toUpperCase(Locale.ROOT)));
                    } catch (IllegalArgumentException ignored) {}
                }
            }
            case CHICKEN, COW, PIG, PARROT -> {
                // Variants for these animals exist, but many are immutable; you can add if needed
            }
        }
    }
}

