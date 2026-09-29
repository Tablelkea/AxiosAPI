package fr.kilian.api.sprite;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.ObjectContents;

import java.util.Objects;

public class Sprite {
    private final Key atlas;
    private final Key sprite;

    private Sprite(Key atlas, Key sprite){

        Objects.requireNonNull(sprite, "sprite cannot be null");

        this.atlas = atlas;
        this.sprite = sprite;

    }

    public Component component() {

        ObjectContents contentSprite;

        if(atlas == null){
            contentSprite = ObjectContents.sprite(sprite);

        }else{
            contentSprite = ObjectContents.sprite(atlas, sprite);
        }

        return Component.object(contentSprite);
    }

    public static Sprite of(String sprite){

        Objects.requireNonNull(sprite, "sprite cannot be null");

        if(sprite.isBlank()){
            throw new IllegalArgumentException("sprite cannot be blank");
        }

        Key spriteKey = Key.key(sprite);

        return new Sprite(null, spriteKey);
    }

    public static Sprite of(String atlas, String sprite){

        Objects.requireNonNull(atlas, "atlas cannot be null");
        Objects.requireNonNull(sprite, "sprite cannot be null");

        if(atlas.isBlank()){
            throw new IllegalArgumentException("atlas cannot be blank");
        }

        if(sprite.isBlank()){
            throw new IllegalArgumentException("sprite cannot be blank");
        }

        Key atlasKey = Key.key(atlas);
        Key spriteKey = Key.key(sprite);

        return new Sprite(atlasKey, spriteKey);
    }

}
