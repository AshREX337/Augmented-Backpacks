package com.ashrex.augmented.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;

public class ClientSoundPlayer {
    private ClientSoundPlayer() {}

    public static void playClientSound(SoundEvent sound)
    {
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(sound, 1.0F, 3.0f)
        );
    }

}
