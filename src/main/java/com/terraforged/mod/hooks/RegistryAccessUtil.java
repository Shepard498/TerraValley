/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.mod.hooks;

import com.mojang.serialization.DynamicOps;
import com.terraforged.mod.Environment;
import com.terraforged.mod.TerraForged;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class RegistryAccessUtil {
    private static final ConcurrentHashMap<Class<?>, Optional<Field>> REGISTRY_FIELD_CACHE = new ConcurrentHashMap<>();

    public static Optional<RegistryAccess> getRegistryAccess(DynamicOps<?> ops) {
        if (!(ops instanceof RegistryOps<?>)) {
            return Optional.empty();
        }

        return getRegistryAccess((RegistryOps<?>) ops);
    }

    public static Optional<RegistryAccess> getRegistryAccess(RegistryOps<?> ops) {
        if (ops.lookupProvider instanceof RegistryOps.HolderLookupAdapter adapter
                && adapter.lookupProvider instanceof RegistryAccess access) {
            return Optional.of(access);
        }

        return Optional.of(new LookupBackedAccess(ops.lookupProvider));
    }

    private static Optional<Registry<?>> findRegistry(Object value) {
        if (value instanceof Registry<?> registry) {
            return Optional.of(registry);
        }

        return REGISTRY_FIELD_CACHE.computeIfAbsent(value.getClass(), RegistryAccessUtil::findRegistryField)
                .flatMap(field -> readRegistryField(field, value));
    }

    private static Optional<Field> findRegistryField(Class<?> type) {
        while (type != null) {
            var field = Arrays.stream(type.getDeclaredFields())
                    .filter(f -> Registry.class.isAssignableFrom(f.getType()))
                    .findFirst();

            if (field.isPresent()) {
                field.get().setAccessible(true);
                return field;
            }

            type = type.getSuperclass();
        }

        return Optional.empty();
    }

    private static Optional<Registry<?>> readRegistryField(Field field, Object value) {
        try {
            return Optional.ofNullable((Registry<?>) field.get(value));
        } catch (IllegalAccessException e) {
            return Optional.empty();
        }
    }

    private record LookupBackedAccess(RegistryOps.RegistryInfoLookup lookup) implements RegistryAccess {
        @Override
        public <E> Optional<Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> key) {
            return lookup.lookup(key)
                    .flatMap(info -> findRegistry(info.owner()).or(() -> findRegistry(info.getter())))
                    .map(registry -> (Registry<E>) registry);
        }

        @Override
        public Stream<RegistryEntry<?>> registries() {
            return Stream.empty();
        }
    }

    public static <T> MappedRegistry<T> copy(Registry<T> input) {
        var copy = new MappedRegistry<>(input.key(), input.registryLifecycle());
        for (var value : input) {
            var key = input.getResourceKey(value).orElseThrow();
            var registrationInfo = input.registrationInfo(key).orElse(RegistrationInfo.BUILT_IN);
            copy.register(key, value, registrationInfo);
        }
        return copy;
    }

    public static <T> void copy(Registry<T> registry, WritableRegistry<T> dest) {
        for (var entry : registry.entrySet()) {
            var registrationInfo = registry.registrationInfo(entry.getKey()).orElse(RegistrationInfo.BUILT_IN);
            dest.register(entry.getKey(), entry.getValue(), registrationInfo);
        }
    }

    public static void printRegistryContents(Registry<?> registry) {
        if (!Environment.DEBUGGING) return;

        TerraForged.LOG.info(" - Registry: {}, Size: {}", registry.key().location(), registry.size());
        for (var entry : registry.entrySet()) {
            TerraForged.LOG.info("  - {}", entry.getKey().location());
        }
    }
}
