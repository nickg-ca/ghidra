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
    *   Manages the current context (`McpContext`) including the active program.

2.  **Tools (`ghidra.mcp.tools`)**:
    *   Expose specific Ghidra functionalities as MCP Tools.
    *   **Core Tools:**
        *   `list_tools_info`: Lists available tools.
    *   **Project Management Tools:**
        *   `list_project_files`: Lists all files in the current Ghidra project.
        *   `open_program`: Opens a specific program from the project.
        *   `close_program`: Closes the current program.
    *   **Reverse Engineering Tools:**
        *   `get_listing`: Retrieves disassembly listing for a range of addresses.
        *   `decompile_function`: Decompiles a function at a given address.
        *   `get_symbol`: Retrieves symbol information for an address.
        *   `read_bytes`: Reads raw bytes from memory at a given address.
    *   **Modification Tools:**
        *   `set_label`: Renames or creates a label (symbol) at a specific address.
        *   `set_comment`: Adds or modifies comments (plate, pre, eol, post, repeatable) at an address.
        *   `write_bytes`: Writes raw bytes to memory at a specific address.

3.  **Resources (`ghidra.mcp.resources`)**:
    *   Expose data as MCP Resources (read-only data sources).
    *   **Implemented Resources:**
        *   `program://current/listing`: Access to the current program's listing.

4.  **Integration (`ghidra_scripts/RunMCPServer.java`)**:
    *   A Ghidra Script to launch the server.
    *   Designed to be run in Headless mode (e.g., via `./analyzeHeadless`).
    *   Keeps the process alive to maintain the server connection.
    *   Supports running with or without an initial binary.

## Usage

To run the MCP server, use Ghidra's headless analyzer with the `RunMCPServer.java` script.

### Mode 1: With a specific binary
```bash
<ghidra_install>/support/analyzeHeadless <project_path> <project_name> -process <binary_name> -postScript RunMCPServer.java
```

### Mode 2: Project Mode (No initial binary)
```bash
<ghidra_install>/support/analyzeHeadless <project_path> <project_name> -noanalysis -preScript RunMCPServer.java
```
*Note: In this mode, use the `open_program` tool to load a binary.*

Ensure the MCP client is configured to spawn this process and communicate via stdin/stdout.

## Future Work

*   **Prompts:** Implement MCP Prompts to guide the AI in specific reverse engineering workflows.
*   **More Tools:** Add support for:
    *   Running arbitrary scripts.
*   **Authentication:** Implement authentication if exposing over network (currently local stdio only).
