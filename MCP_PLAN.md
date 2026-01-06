# Ghidra MCP Server Plan

This document outlines the plan and architecture for the Model Context Protocol (MCP) Server integration in Ghidra.

## Goal

Create a local MCP server mode for Ghidra using the `stdio` interface. This allows MCP clients (like Claude Desktop or other AI assistants) to directly interact with Ghidra's reverse engineering capabilities, such as reading memory, listing instructions, and decompiling functions.

## Architecture

### Module Structure

*   **Path:** `Ghidra/Features/MCP`
*   **Dependencies:**
    *   `Ghidra/Features/Base` (for Ghidra API)
    *   `Ghidra/Features/Decompiler` (for decompilation)
    *   `io.modelcontextprotocol.sdk:mcp` (Java SDK for MCP)

### Core Components

1.  **MCPServer (`ghidra.mcp.MCPServer`)**:
    *   The main entry point.
    *   Initializes the `StdioServerTransport` for JSON-RPC communication over standard input/output.
    *   Redirects Ghidra's `System.out` to `System.err` to ensure protocol messages on `stdout` are not corrupted by Ghidra logs.
    *   Registers tools and resources.

2.  **Tools (`ghidra.mcp.tools`)**:
    *   Expose specific Ghidra functionalities as MCP Tools.
    *   **Implemented Tools:**
        *   `list_tools_info`: Lists available tools (meta-tool).
        *   `get_listing`: Retrieves disassembly listing for a range of addresses.
        *   `decompile_function`: Decompiles a function at a given address.
        *   `get_symbol`: Retrieves symbol information for an address.

3.  **Resources (`ghidra.mcp.resources`)**:
    *   Expose data as MCP Resources (read-only data sources).
    *   **Implemented Resources:**
        *   `program://current/listing`: Access to the current program's listing.

4.  **Integration (`ghidra_scripts/RunMCPServer.java`)**:
    *   A Ghidra Script to launch the server.
    *   Designed to be run in Headless mode (e.g., via `./analyzeHeadless`).
    *   Keeps the process alive to maintain the server connection.

## Usage

To run the MCP server, use Ghidra's headless analyzer with the `RunMCPServer.java` script.

```bash
<ghidra_install>/support/analyzeHeadless <project_path> <project_name> -process <binary_name> -postScript RunMCPServer.java
```

Ensure the MCP client is configured to spawn this process and communicate via stdin/stdout.

## Future Work

*   **Prompts:** Implement MCP Prompts to guide the AI in specific reverse engineering workflows.
*   **More Tools:** Add support for:
    *   Renaming symbols (`set_label`).
    *   Adding comments (`set_comment`).
    *   Reading/Writing bytes (`read_bytes`, `write_bytes`).
    *   Running arbitrary scripts.
*   **Authentication:** Implement authentication if exposing over network (currently local stdio only).
