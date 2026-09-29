package fr.kilian.api.sprite;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.ObjectContents;

import java.util.Objects;

public record Sprite(
        Key atlas,
        Key sprite
) {


    public Sprite{

        Objects.requireNonNull(atlas, "atlas cannot be null");
        Objects.requireNonNull(sprite, "sprite cannot be null");


    }

    public Component component() {
        ObjectContents contentSprite = ObjectContents.sprite(atlas, sprite);
        return Component.object(contentSprite);
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
        
        return new Sprite(Key.key(atlas), Key.key(sprite));
    }

}
