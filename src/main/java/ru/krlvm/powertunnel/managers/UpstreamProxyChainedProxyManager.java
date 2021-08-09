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

package ru.krlvm.powertunnel.managers;

import org.littleshoot.proxy.ChainedProxy;
import org.littleshoot.proxy.ChainedProxyManager;

import java.net.UnknownHostException;
import java.util.Queue;

import io.netty.handler.codec.http.HttpRequest;
import org.littleshoot.proxy.impl.ClientDetails;
import ru.krlvm.powertunnel.PowerTunnel;
import ru.krlvm.powertunnel.adapters.UpstreamChainedProxyAdapter;

public class UpstreamProxyChainedProxyManager implements ChainedProxyManager {

    private UpstreamChainedProxyAdapter adapter = null;

    public UpstreamProxyChainedProxyManager() {
        if(PowerTunnel.UPSTREAM_PROXY_CACHE) {
            try {
                adapter = new UpstreamChainedProxyAdapter(PowerTunnel.resolveUpstreamProxyAddress());
            } catch (UnknownHostException ex) {
                System.out.println("[x] Failed to cache upstream proxy address - resolution failed: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void lookupChainedProxies(HttpRequest httpRequest, Queue<ChainedProxy> chainedProxies, ClientDetails clientDetails) {
        chainedProxies.add(adapter != null ? adapter : new UpstreamChainedProxyAdapter());
    }
}
