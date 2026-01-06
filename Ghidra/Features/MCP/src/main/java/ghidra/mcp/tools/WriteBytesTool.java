package ghidra.mcp.tools;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.Gson;

import ghidra.mcp.McpContext;
import ghidra.mcp.McpTool;
import ghidra.program.model.listing.Program;
import ghidra.program.model.mem.Memory;
import ghidra.program.model.address.Address;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class WriteBytesTool implements McpTool {

    private static final Gson gson = new Gson();

    @Override
    public Tool getToolDef() {
        Map<String, Object> schema = Map.of(
            "type", "object",
            "properties", Map.of(
                "address", Map.of("type", "string", "description", "Address to write bytes to"),
                "bytes", Map.of("type", "string", "description", "Hex string of bytes to write")
            ),
            "required", List.of("address", "bytes")
        );

        return new Tool(
            "write_bytes",
            "Write bytes to memory at a specific address",
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
                String hexBytes = (String) args.get("bytes");

                if (hexBytes == null || hexBytes.isEmpty()) {
                    return new CallToolResult(List.of(new TextContent("Error: 'bytes' argument is required and cannot be empty")), true);
                }

                if (hexBytes.length() % 2 != 0) {
                     return new CallToolResult(List.of(new TextContent("Error: Hex string length must be even")), true);
                }

                Address addr = program.getAddressFactory().getAddress(addrStr);
                if (addr == null) {
                     return new CallToolResult(
                        List.of(new TextContent("Invalid address: " + addrStr)),
                        true
                    );
                }

                byte[] bytes;
                try {
                    bytes = hexStringToByteArray(hexBytes);
                } catch (IllegalArgumentException e) {
                    return new CallToolResult(List.of(new TextContent("Error: Invalid hex string: " + e.getMessage())), true);
                }

                Memory memory = program.getMemory();

                // We need a transaction to modify the program
                int tid = program.startTransaction("Write Bytes");
                try {
                    memory.setBytes(addr, bytes);
                } finally {
                    program.endTransaction(tid, true);
                }

                return new CallToolResult(
                    List.of(new TextContent("Wrote " + bytes.length + " bytes to " + addrStr)),
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

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int digit1 = Character.digit(s.charAt(i), 16);
            int digit2 = Character.digit(s.charAt(i+1), 16);
            if (digit1 == -1 || digit2 == -1) {
                throw new IllegalArgumentException("Non-hex character at index " + i);
            }
            data[i / 2] = (byte) ((digit1 << 4) + digit2);
        }
        return data;
    }
}
