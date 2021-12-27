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

package io.github.krlvm.powertunnel.adapters;

import io.github.krlvm.powertunnel.filters.ProxyFilter;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;

public class ProxyFiltersSourceAdapter extends HttpFiltersSourceAdapter {

    private final ProxyListener listener;
    private final boolean isFullRequest, isFullResponse;

    public ProxyFiltersSourceAdapter(ProxyListener listener, boolean isFullRequest, boolean isFullResponse) {
        this.listener = listener;
        this.isFullRequest = isFullRequest;
        this.isFullResponse = isFullResponse;
    }

    @Override
    public HttpFilters filterRequest(HttpRequest originalRequest) {
        return new ProxyFilter(this.listener, originalRequest);
    }

    @Override
    public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
        return new ProxyFilter(this.listener, originalRequest/*, ctx*/);
    }

    @Override
    public int getMaximumRequestBufferSizeInBytes() {
        return this.isFullRequest ? 10 * 1024 * 1024 : super.getMaximumResponseBufferSizeInBytes();
    }

    @Override
    public int getMaximumResponseBufferSizeInBytes() {
        return this.isFullResponse ? 10 * 1024 * 1024 : super.getMaximumResponseBufferSizeInBytes();
    }
}
