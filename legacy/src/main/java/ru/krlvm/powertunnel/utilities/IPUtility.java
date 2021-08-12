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

package ru.krlvm.powertunnel.utilities;

/**
 * Utility for working with IP addresses
 *
 * @author krlvm
 */
public class IPUtility {

    /**
     * Retrieves is an IP address is an IPv6 address
     *
     * Very dirty implementation, needs to be rewritten
     * That is needed because we support (DNS) both IPv4 and IPv6
     * addresses, and they may have a specified port
     *
     * IPv4 example: 127.0.0.1
     *    with port: 127.0.0.1:8085
     * IPv6 example: ::1
     *    with port: [::1]:8085
     *
     * We will count occurrences of ":" in an address
     *
     * @param address - an IP address
     * @return true if it's IPv6 or false if it is IPv4 (or invalid)
     */
    public static boolean isIPv6(String address) {
        return address.replaceAll("[^:]", "").length() >= 2;
    }

    public static boolean isIPv6WithPort(String address) {
        return isIPv6(address) && address.contains("[") && address.contains("]:");
    }

    public static Object[] splitIPv6(String address) {
        if(!isIPv6WithPort(address)) {
            return null;
        }
        int index = address.lastIndexOf(":");
        try {
            return new Object[]{
                    address.substring(0, index).replace("[", "").replace("]", ""),
                    Integer.parseInt(address.substring(index+1))
            };
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static boolean isIPv4(String address) {
        return !isIPv6(address);
    }

    public static boolean isIPv4WithPort(String address) {
        return isIPv4(address) && address.contains(":");
    }

    public static Object[] splitIPv4(String address) {
        if(!isIPv4WithPort(address)) {
            return null;
        }
        int index = address.lastIndexOf(":");
        try {
            return new Object[]{
                    address.substring(0, index),
                    Integer.parseInt(address.substring(index+1))
            };
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static Object[] split(String address) {
        if(!hasPort(address)) {
            return null;
        }
        if(isIPv6WithPort(address)) {
            return splitIPv6(address);
        } else if(isIPv4WithPort(address)) {
            return splitIPv4(address);
        } else {
            return null;
        }
    }

    public static boolean hasPort(String address) {
        return isIPv6WithPort(address) || isIPv4WithPort(address);
    }

    public static boolean isValid(String address) {
        return isIPv4(address) || isIPv4WithPort(address)
                || isIPv6(address) || isIPv6WithPort(address);
    }
}
