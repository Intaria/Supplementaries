package net.mehvahdjukaar.supplementaries.integration.forge;

import net.mehvahdjukaar.supplementaries.common.block.IKeyLockable;
import net.mehvahdjukaar.supplementaries.common.block.tiles.KeyLockableTile;
import net.mehvahdjukaar.supplementaries.common.items.KeyItem;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import org.jetbrains.annotations.Nullable;

public class CuriosCompatImpl {
    public static KeyLockableTile.KeyStatus isKeyInCurio(Player player, String password) {
        var found = CuriosApi.getCuriosHelper().findCurios(player, i -> {
           return i.is(ModTags.KEY) || i.getItem() instanceof KeyItem;
        });
        if (found.isEmpty()) return KeyLockableTile.KeyStatus.NO_KEY;
        else {
            for (var slot : found) {
                ItemStack stack = slot.stack();
                if (IKeyLockable.getKeyStatus(stack, password).isCorrect()){
                    return KeyLockableTile.KeyStatus.CORRECT_KEY;
                }
            }
            return KeyLockableTile.KeyStatus.INCORRECT_KEY;
        }
    }
}
