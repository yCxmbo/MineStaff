package me.ycxmbo.mineStaff.audit;

import me.ycxmbo.mineStaff.MineStaff;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class JsonAuditLogger {
    private final MineStaff plugin;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public JsonAuditLogger(MineStaff plugin) { this.plugin = plugin; }

    public void log(Map<String, Object> obj) {
        if (!plugin.getConfigManager().getConfig().getBoolean("audit.enabled", true)) return;
        String prefix = plugin.getConfigManager().getConfig().getString("audit.file_prefix", "actions-");
        File f = new File(plugin.getDataFolder(), prefix + LocalDate.now().format(fmt) + ".log");
        f.getParentFile().mkdirs();
        try (FileWriter fw = new FileWriter(f, true)) {
            fw.write(toJson(obj));
            fw.write("\n");
        } catch (IOException ignored) {}
    }

    private String escape(String s) { return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\""); }

    private String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        boolean first = true;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append('"').append(escape(e.getKey())).append('"').append(':');
            Object v = e.getValue();
            if (v == null) sb.append("null");
            else if (v instanceof Number || v instanceof Boolean) sb.append(String.valueOf(v));
            else sb.append('"').append(escape(String.valueOf(v))).append('"');
        }
        sb.append('}');
        return sb.toString();
    }
}

