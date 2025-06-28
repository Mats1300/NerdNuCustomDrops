package nu.nerd.utils;

import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class EntityVariantUtils {

    /**
     * Gets a string representing the "variant" or subtype of the entity.
     * This includes variant, color, gene, type, profession, or behavior.
     *
     * @param entity the entity to inspect
     * @return a string variant ID (like "angry", "red", "tabby", etc.), or null if none
     */
    @Nullable
    public static String getVariantId(Entity entity) {
        if (entity instanceof Wolf wolf) {
            String variant = wolf.getVariant().toString().toLowerCase();
            if (wolf.isAngry()) variant = "angry_" + variant;
            return variant;
        } else if (entity instanceof Axolotl axolotl) {
            return axolotl.getVariant().toString().toLowerCase();
        } else if (entity instanceof Frog frog) {
            return frog.getVariant().toString().toLowerCase();
        } else if (entity instanceof Chicken chicken) {
            return chicken.getVariant().toString().toLowerCase();
        } else if (entity instanceof Cow cow && !(cow instanceof MushroomCow)) {
            return cow.getVariant().toString().toLowerCase();
        } else if (entity instanceof Pig pig) {
            return pig.getVariant().toString().toLowerCase();
        } else if (entity instanceof MushroomCow mooshroom) {
            return mooshroom.getVariant().toString().toLowerCase();
        } else if (entity instanceof Parrot parrot) {
            return parrot.getVariant().toString().toLowerCase();
        } else if (entity instanceof Sheep sheep) {
            Component nameComponent = sheep.customName();
            String name = nameComponent != null ? PlainTextComponentSerializer.plainText().serialize(nameComponent) : null;
            if ("jeb_".equals(name)) {
                return "jeb_";
            }
            return sheep.getColor() != null ? sheep.getColor().name().toLowerCase() : null;
        } else if (entity instanceof Horse horse) {
            return horse.getColor().name().toLowerCase();
        } else if (entity instanceof Llama llama) {
            if (llama.getType() == EntityType.TRADER_LLAMA) {
                return "trader_llama_" + llama.getColor().name().toLowerCase();
            } else {
                return "llama_" + llama.getColor().name().toLowerCase();
            }
        } else if (entity instanceof Cat cat) {
            return cat.getCatType().name().toLowerCase();
        } else if (entity instanceof Bee bee) {
            return bee.getAnger() > 0 ? "angry" : "calm";
        } else if (entity instanceof Fox fox) {
            return fox.getFoxType().toString().toLowerCase();
        } else if (entity instanceof Ghast ghast) {
            return ghast.isCharging() ? "charging" : "idle";
        } else if (entity instanceof HappyGhast happyGhast) {
            ItemStack saddle = happyGhast.getEquipment().getItem(EquipmentSlot.CHEST);
            if (saddle.getType() == Material.SADDLE) {
                return "saddled";
            } else {
                return "happy";
            }
        } else if (entity instanceof Goat goat) {
            return goat.isScreaming() ? "screaming" : "normal";
            } else if (entity instanceof Panda panda) {
                return panda.getMainGene().toString().toLowerCase();
            } else if (entity instanceof Rabbit rabbit) {
                return rabbit.getRabbitType().toString().toLowerCase();
            } else if (entity instanceof Snowman snowman) {
                return snowman.isDerp() ? "derp" : "normal";
            } else if (entity instanceof Strider strider) {
                return strider.isShivering() ? "shivering" : "normal";
            } else if (entity instanceof Villager villager) {
                return villager.getProfession().name().toLowerCase();
            } else if (entity instanceof ZombieVillager zombieVillager) {
                return zombieVillager.getVillagerProfession() != Villager.Profession.NONE
                        ? zombieVillager.getVillagerProfession().name().toLowerCase()
                        : null;
            }

            return null;
        }
    }

