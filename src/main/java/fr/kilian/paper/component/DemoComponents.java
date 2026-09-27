package fr.kilian.paper.component;

import fr.kilian.api.component.ComponentCodec;
import fr.kilian.api.component.ComponentKey;

public final class DemoComponents {

    public static final ComponentKey<Integer> LOGIN_COUNT =
            new ComponentKey<>(
                    "axiosapi",
                    "login_count",
                    Integer.class
            );

    private DemoComponents() {
    }

    public final class IntegerComponentCodec
            implements ComponentCodec<Integer> {

        @Override
        public String encode(Integer value) {
            return Integer.toString(value);
        }

        @Override
        public Integer decode(String payload) {
            return Integer.parseInt(payload);
        }
    }
}
