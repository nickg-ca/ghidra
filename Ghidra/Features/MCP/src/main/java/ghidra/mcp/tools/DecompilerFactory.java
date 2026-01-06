package ghidra.mcp.tools;

import ghidra.app.decompiler.DecompInterface;

public interface DecompilerFactory {
    DecompilerWrapper create();
}
