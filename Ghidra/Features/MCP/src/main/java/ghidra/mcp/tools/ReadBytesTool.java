package ghidra.mcp.tools;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.Gson;

import ghidra.framework.plugintool.PluginTool;
import ghidra.mcp.McpContext;
import ghidra.mcp.McpTool;
import ghidra.program.model.listing.Program;
import ghidra.program.model.mem.Memory;
import ghidra.program.model.address.Address;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class ReadBytesTool implements McpTool {

    private static final Gson gson = new Gson();

    @Override
    public Tool getToolDef() {
        Map<String, Object> schema = Map.of(
            "type", "object",
            "properties", Map.of(
                "address", Map.of("type", "string", "description", "Start address"),
                "length", Map.of("type", "integer", "description", "Number of bytes to read")
            ),
            "required", List.of("address", "length")
        );

        return new Tool(
            "read_bytes",
            "Read raw bytes from memory",
            gson.toJson(schema)
        );
    }

    @Override
    public Function<Map<String, Object>, CallToolResult> getHandler(McpContext context) {
        return args -> {
            try {
                Program program = context.getCurrentProgram();
                if (program == null) {
                    return new CallToolResult(List.of(new TextContent("No active program.")), true);
                }

                String addrStr = (String) args.get("address");
                int length = ((Number) args.get("length")).intValue();

                Address startAddr = program.getAddressFactory().getAddress(addrStr);
                if (startAddr == null) {
                     return new CallToolResult(
                        List.of(new TextContent("Invalid address: " + addrStr)),
                        true
                    );
                }

                Memory memory = program.getMemory();
                byte[] bytes = new byte[length];
                int bytesRead = memory.getBytes(startAddr, bytes);

                StringBuilder hex = new StringBuilder();
                for (int i = 0; i < bytesRead; i++) {
                    hex.append(String.format("%02X", bytes[i]));
                }

                return new CallToolResult(
                    List.of(new TextContent(hex.toString())),
                    false
                );

            } catch (Exception e) {
                return new CallToolResult(
                    List.of(new TextContent("Error: " + e.getMessage())),
                    true
                );
            }
        };
    }
}
