/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.sophisticated.activation;

import java.util.List;
import java.util.Map;

/** Shared activation state for the exact Sophisticated family profiles. */
public final class SophisticatedRuntime {

    public static final String CORE = "core";
    public static final String STORAGE = "storage";
    public static final String BACKPACKS = "backpacks";
    public static final SophisticatedRuntime INSTANCE = new SophisticatedRuntime();

    private final Map<String, RouteActivation> routes = Map.of(
            CORE, new RouteActivation(CORE),
            STORAGE, new RouteActivation(STORAGE),
            BACKPACKS, new RouteActivation(BACKPACKS)
    );

    private SophisticatedRuntime() {
    }

    public RouteActivation route(String routeId) {
        RouteActivation route = routes.get(routeId);
        if (route == null) {
            throw new IllegalArgumentException("unknown Sophisticated route " + routeId);
        }
        return route;
    }

    public List<RouteActivation> routes() {
        return List.copyOf(routes.values());
    }

    public void disableAll(String detail) {
        routes.values().forEach(route -> route.fail(detail));
    }

    public void blockFamiliesIfCoreInactive() {
        if (route(CORE).isActive()) {
            return;
        }
        route(STORAGE).inactive("blocked-by-core");
        route(BACKPACKS).inactive("blocked-by-core");
    }
}
