package com.simpledata.frame.base.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author zcw
 * @version 1.0
 * @date 2021/1/26 10:17
 */
public class Builder<T> {

    private final Supplier<T> instantiator;

    private List<Consumer<T>> modifiers = new ArrayList();

    public Builder(Supplier<T> instant) {
        this.instantiator = instant;
    }

    public static <T> Builder<T> of(Supplier<T> instant) {
        return new Builder<T>(instant);
    }

    public <P> Builder<T> with(ParamsFunction<T, P> consumer, P p) {
        Consumer<T> c = instance -> consumer.accept(instance, p);
        modifiers.add(c);
        return this;
    }

    public T build() {
        T value = instantiator.get();
        modifiers.forEach(modifier -> modifier.accept(value));
        modifiers.clear();
        return value;
    }

    @FunctionalInterface
    public interface ParamsFunction<T, P> {
        /**
         * 接收参数方法
         * @param t 对象
         * @param p 参数
         */
        void accept(T t, P p);
    }



}
