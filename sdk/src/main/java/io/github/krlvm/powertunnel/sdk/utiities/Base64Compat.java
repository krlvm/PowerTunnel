/*
 * This file is part of PowerTunnel.
 *
 * PowerTunnel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PowerTunnel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PowerTunnel.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.krlvm.powertunnel.sdk.utiities;

import java.lang.reflect.Method;
import java.util.Base64;

public final class Base64Compat {

    public static String encodeToString(byte[] data) {
        return provider.encodeToString(data);
    }
    public static String encodeURLToString(byte[] data) {
        return provider.encodeURLToString(data);
    }

    private static Base64Provider provider;
    private interface Base64Provider {
        String encodeToString(byte[] data);
        String encodeURLToString(byte[] data);
    }
    private static final class Java8Base64Provider implements Base64Provider {
        @Override
        public String encodeToString(byte[] data) {
            return Base64.getEncoder().encodeToString(data);
        }
        @Override
        public String encodeURLToString(byte[] data) {
            return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
        }
    }
    private static final class AndroidBase64Provider implements Base64Provider {
        private final Method method;

        public AndroidBase64Provider(Class<?> clazz) throws ReflectiveOperationException {
            method = clazz.getDeclaredMethod("encodeToString", byte[].class, int.class);
        }

        @Override
        public String encodeToString(byte[] data) {
            try {
                return ((String) method.invoke(null, data, 0));
            } catch (Throwable t) {
                throw new RuntimeException("Failed to invoke Base64.encodeToString", t);
            }
        }
        @Override
        public String encodeURLToString(byte[] data) {
            try {
                // URL_SAFE | NO_PADDING | NO_WRAP
                return ((String) method.invoke(null, data, 0x00000008 | 0x00000001 | 0x00000002));
            } catch (Throwable t) {
                throw new RuntimeException("Failed to invoke Base64.encodeToString", t);
            }
        }
    }
    private static final class DummyBase64Provider implements Base64Provider {
        @Override
        public String encodeToString(byte[] data) {
            throw new RuntimeException("Not supported on your platform");
        }
        @Override
        public String encodeURLToString(byte[] data) {
            throw new RuntimeException("Not supported on your platform");
        }
    }
    static {
        try {
            Class.forName("java.util.Base64");
            provider = new Java8Base64Provider();
        } catch (ClassNotFoundException ex) {
            try {
                provider = new AndroidBase64Provider(Class.forName("android.util.Base64"));
            } catch (ReflectiveOperationException aex) {
                System.err.println("Base64 is not supported on your platform\nSome functionality can be unavailable");
                provider = new DummyBase64Provider();
            }
        }
    }
}
