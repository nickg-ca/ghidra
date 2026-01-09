# Ghidra MCP Server

This module provides a Model Context Protocol (MCP) server for Ghidra, allowing external AI agents to interact with the Ghidra reverse engineering platform.

## Architecture

The MCP server runs as a Ghidra Script (`RunMCPServer.java`) inside the Ghidra process. It exposes a **Streamable HTTP** interface on `localhost:31337`.

- **Transport**: Streamable HTTP (SSE for notifications, POST for requests).
- **Authentication**: Bearer Token (generated on startup).
- **Port**: 31337 (Fixed).

## Usage

1.  Open Ghidra and load your project/program.
2.  Open the Script Manager (Window -> Script Manager).
3.  Locate `RunMCPServer.java` under the `MCP` category.
4.  Run the script.
5.  Check the **Console** (or standard error) for the startup message:
    ```
    Starting MCP Server on port 31337...
    Authentication Token: <GENERATED_TOKEN>
    Ensure you pass 'Authorization: Bearer <GENERATED_TOKEN>' header in your requests.
    ```
6.  Configure your MCP Client to connect to `http://localhost:31337`.
    -   Add the HTTP Header: `Authorization: Bearer <GENERATED_TOKEN>`.

## Endpoints

The server listens on `http://localhost:31337/`. Standard MCP Streamable HTTP endpoints are handled:

-   `/sse`: Server-Sent Events endpoint for notifications and connection establishment.
-   `/messages`: Endpoint for sending JSON-RPC messages (requests).

## Available Tools

The server exposes the following tools to the LLM:
-   `list_modules`: List open programs.
-   `read_bytes`: Read memory.
-   `decompile`: Decompile functions.
-   `get_listing`: Get assembly listing.
-   `get_references`: Find references.
-   `set_comment`: Add comments.
-   `set_label`: Rename labels.
-   And more...

## Development

-   **Build**: `./gradlew :MCP:build`
-   **Dependencies**: Requires `jakarta.servlet-api`, `jetty-server`, `jetty-servlet`, and the `mcp-sdk`.
