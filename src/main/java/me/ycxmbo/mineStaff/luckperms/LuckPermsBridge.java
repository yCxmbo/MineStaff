package me.ycxmbo.mineStaff.luckperms;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.List;

/** Lightweight LuckPerms helper using reflection to avoid hard dep. */
public class LuckPermsBridge {
    private static Object api() {
        try {
            Class<?> prov = Class.forName("net.luckperms.api.LuckPermsProvider");
            Method get = prov.getMethod("get");
            return get.invoke(null);
        } catch (Throwable t) { return null; }
    }

    public static boolean addTempPerms(Player p, List<String> nodes) {
        Object lp = api(); if (lp == null) return false;
        try {
            Class<?> LuckPerms = Class.forName("net.luckperms.api.LuckPerms");
            Method getUserManager = LuckPerms.getMethod("getUserManager");
            Object userManager = getUserManager.invoke(lp);
            Method loadUser = userManager.getClass().getMethod("loadUser", java.util.UUID.class);
            Object stage = loadUser.invoke(userManager, p.getUniqueId());
            // CompletableFuture<User>
            Class<?> CompletableFuture = Class.forName("java.util.concurrent.CompletableFuture");
            Method join = CompletableFuture.getMethod("join");
            Object user = join.invoke(stage);

            Class<?> Node = Class.forName("net.luckperms.api.node.Node");
            Class<?> NodeBuilder = Class.forName("net.luckperms.api.node.NodeBuilders");
            Method permBuilder = NodeBuilder.getMethod("permission", String.class);
            Method build = Class.forName("net.luckperms.api.node.NodeBuilder").getMethod("build");
            Method data = user.getClass().getMethod("data");
            Object userData = data.invoke(user);
            Method add = userData.getClass().getMethod("add", Node);
            for (String n : nodes) {
                Object builder = permBuilder.invoke(null, n);
                Object node = build.invoke(builder);
                add.invoke(userData, node);
            }
            Method save = userManager.getClass().getMethod("saveUser", Class.forName("net.luckperms.api.model.user.User"));
            save.invoke(userManager, user);
            return true;
        } catch (Throwable t) { return false; }
    }

    public static boolean removePerms(Player p, List<String> nodes) {
        Object lp = api(); if (lp == null) return false;
        try {
            Class<?> LuckPerms = Class.forName("net.luckperms.api.LuckPerms");
            Method getUserManager = LuckPerms.getMethod("getUserManager");
            Object userManager = getUserManager.invoke(lp);
            Method loadUser = userManager.getClass().getMethod("loadUser", java.util.UUID.class);
            Object stage = loadUser.invoke(userManager, p.getUniqueId());
            Class<?> CompletableFuture = Class.forName("java.util.concurrent.CompletableFuture");
            Method join = CompletableFuture.getMethod("join");
            Object user = join.invoke(stage);

            Class<?> Node = Class.forName("net.luckperms.api.node.Node");
            Class<?> NodeBuilder = Class.forName("net.luckperms.api.node.NodeBuilders");
            Method permBuilder = NodeBuilder.getMethod("permission", String.class);
            Method build = Class.forName("net.luckperms.api.node.NodeBuilder").getMethod("build");
            Method data = user.getClass().getMethod("data");
            Object userData = data.invoke(user);
            Method remove = userData.getClass().getMethod("remove", Node);
            for (String n : nodes) {
                Object builder = permBuilder.invoke(null, n);
                Object node = build.invoke(builder);
                remove.invoke(userData, node);
            }
            Method save = userManager.getClass().getMethod("saveUser", Class.forName("net.luckperms.api.model.user.User"));
            save.invoke(userManager, user);
            return true;
        } catch (Throwable t) { return false; }
    }
}

