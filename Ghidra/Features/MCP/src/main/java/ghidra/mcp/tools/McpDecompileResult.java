package ghidra.mcp.tools;

public interface McpDecompileResult {
    boolean decompileCompleted();
    String getErrorMessage();
    String getC();
}
