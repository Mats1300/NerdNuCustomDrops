package nu.nerd.utils;

import org.bukkit.entity.*;
import org.jetbrains.annotations.Nullable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;


/**
 * Utility class for retrieving variant or subtype identifiers for Minecraft entities.
 * <p>
 * This class provides a single method to determine a "variant" string for an entity.
 * Variants may include:
 * <ul>
 *     <li>Color (e.g., Sheep, Horse)</li>
 *     <li>Behavior (e.g., Wolf angry, Bee angry)</li>
 *     <li>Type or breed (e.g., Cat, Rabbit, Llama)</li>
 *     <li>Special identifiers (e.g., Jeb_ sheep, Trader Llama)</li>
 *     <li>Profession (e.g., Villager, ZombieVillager)</li>
 * </ul>
 * If an entity has no variant, the method returns {@code null}.
 */
public class EntityVariantUtils {

    /**
     * Returns a string representing the "variant" or subtype of the given entity.
     * <p>
     * Examples of returned strings:
     * <ul>
     *     <li>Wolf: "pale", "ashen", "black", "chestnut"</li>
     *     <li>Sheep: "white", "black", "jeb_"</li>
     *     <li>Cat: "tabby", "black"</li>
     *     <li>Villager: "farmer", "librarian"</li>
     *     <li>Fox: "red", "snow"</li>
     * </ul>
     *
     * @param entity the {@link Entity} to inspect
     * @return a lowercase string variant ID (color, type, behavior, or profession),
     *         or {@code null} if the entity has no variant
     */
    @Nullable
    @SuppressWarnings("ConstantConditions")
    public static String getVariantId(Entity entity) {
        if (entity instanceof Wolf wolf) {
            String variant = wolf.getVariant().getKey().getKey(); // "pale", "ashen", "black", "chestnut"
            if (wolf.isAngry()) variant = "angry_" + variant;
            return variant;
        } else if (entity instanceof Axolotl axolotl) {
            return axolotl.getVariant().toString().toLowerCase();
        } else if (entity instanceof Frog frog) {
            return frog.getVariant().toString().toLowerCase();
        } else if (entity instanceof Chicken chicken) {
            return chicken.getVariant().getKey().getKey(); // cold, temperate, warm
        } else if (entity instanceof Cow cow) {
            return cow.getVariant().getKey().getKey(); // cold, temperate, warm
        } else if (entity instanceof Pig pig) {
            return pig.getVariant().getKey().getKey(); // cold, temperate, warm
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
            // For normal Llamas:
            if (llama.getType() == EntityType.LLAMA) {
                return llama.getColor().name().toLowerCase(); // "brown", "creamy", etc.
            }
            // For Trader Llamas:
            else if (llama.getType() == EntityType.TRADER_LLAMA) {
                return llama.getColor().name().toLowerCase(); // "brown", "creamy", etc.
            }
        } else if (entity instanceof Cat cat) {
            return cat.getCatType().getKey().getKey();
        } else if (entity instanceof Bee bee) {
            return bee.getAnger() > 0 ? "angry" : "calm";
        } else if (entity instanceof Fox fox) {
            return fox.getFoxType().toString().toLowerCase();
        } else if (entity instanceof Ghast ghast) {
            return ghast.isCharging() ? "shooting" : "idle";
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
                return villager.getProfession().getKey().getKey();
        } else if (entity instanceof ZombieVillager zombieVillager) {
            Villager.Profession profession = zombieVillager.getVillagerProfession();

            if (profession == null) {
                return "nitwit"; // fallback for no profession
            }

            return profession.getKey().getKey();
        }


        // Return null if entity has no variant
        return null;
        }
    }

