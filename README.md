# http4k MCP Desktop Client

[![http4k Logo](http4k-mcp.png)](https://mcp.http4k.org)

A command-line desktop client for connecting to MCP (Model Context Protocol) servers via standard input/output (StdIO). This client is designed to work
seamlessly with AI assistants and applications that communicate through StdIO, such as Claude and other desktop AI applications.

## Overview

The http4k MCP Desktop Client serves as a proxy that enables communication between desktop applications and MCP servers. While it works with any MCP-compliant
server, it's specially optimized for servers built using the [http4k MCP SDK](https://mcp.http4k.org).

## Features

- Multiple remote transport options: HTTP Streaming, SSE (Server-Sent Events), JSON-RPC, WebSocket
- Various authentication methods: API Key, Bearer Token, Basic Auth, and OAuth
- Customizable reconnection strategy
- Simple StdIO interface for easy integration with desktop applications when using natively compiled Kotlin apps.

### Remote Protocol Comparison

The [MCP Standard](https://spec.modelcontextprotocol.io/) remote protocol is HTTP Streaming, which superceded the previous standard (SSE) in the `2025-03-26` version. http4k has implemented other standard transports into the http4k-mcp-desktop, as these will be more appropriate for distributed/hosted MCP servers.

| Protocol           | Standard/Extension | State     | Default server path   | Setting     |Description                                                                                   |
|--------------------|--------------------|-----------|-----------------------|-----------|------------------------------------------------------------------------------------|
| HTTP Non-streaming | MCP Standard       | Stateless | `http://host/mcp`     | `http-nonstream` | Pure HTTP-based streaming communication  **Use this setting for Cursor!**                                                    |
| HTTP Streaming     | MCP Standard       | Stateful  | `http://host/mcp`     | `http-stream` | HTTP/SSE-based streaming communication. Supports sessions and replaying/reconnection of stram |
| SSE                | MCP (Legacy)       | Stateful  | `http://host/sse`     | `sse` | Server-Sent Events, part of HTML5 spec, ideal for streaming data from server to client        |
| WebSocket          | Protocol Extension | Stateful  | `http://host/ws`      | `websocket` | Full-duplex communication protocol, maintains persistent connection                           |
| JSON-RPC           | Protocol Extension | Stateless | `http://host/jsonrpc` | `jsonrpc` | Remote Procedure Call protocol encoded in JSON, request/response model                        |

## Installation

### Via Homebrew

```bash
brew tap http4k/tap
brew install http4k-mcp-desktop
```

### Via GitHub Releases

Download the latest release from [GitHub Releases](https://github.com/http4k/http4k-mcp-desktop/releases).

## Usage

```bash
http4k-mcp-desktop --url http://localhost:3001/<protocol> [OPTIONS]
```

## Command Line Options

| Option             | Description                                                                                                             | Default       |
|--------------------|-------------------------------------------------------------------------------------------------------------------------|---------------|
| `--transport`      | MCP transport mode: `http-stream`, `http-nonstream`, `sse` (legacy), `jsonrpc` (non-streaming), `websocket` (streaming) | `http-stream` |
| `--url`            | URL of the MCP server to connect to (required)                                                                          | N/A           |
| `--reconnectDelay` | Reconnect delay in seconds if disconnected                                                                              | 0             |
| `--version`        | Get the version information for the app                                                                                 | N/A           |

### Authentication Options

At time of writing, the MCP auth standards are in flux, but http4k-mcp-desktop has implemented some standard HTTP mechanisms into the http4k-mcp-desktop.

- Basic Auth
- Bearer Token
- API Key
- *OAuth via standardised auto-discovery

* This is the OAuth standard which is currently in the post 2025-03-26 draft specification which uses auto discovery of the authentication token using 
OAuth Protected Resource and OAuth Client Credentials. This requires:

  - The MCP server to list its authentication servers in it's OAuth Protected Resource metadata endpoint (`/.well-knwon/oauth-protected-resource`)
  - The Authentication server to support the OAuth Client Credentials flow and auto discovery endpoint (`/.well-knwon/oauth-authorization-server`)

MCP servers built using the [http4k MCP SDK](https://mcp.http4k.org) do support the above, but other servers may not.

| Option                     | Description                            | Format                |
|----------------------------|----------------------------------------|-----------------------|
| `--apiKey`                 | API key for server authentication      | String                |
| `--apiKeyHeader`           | Custom header name for API key         | `X-Api-key` (default) |
| `--bearerToken`            | Bearer token for server authentication | String                |
| `--basicAuth`              | Basic authentication credentials       | `<user>:<password>`   |
| `--oauthScopes`            | OAuth scopes to request                | Comma-separated list  |
| `--oauthClientCredentials` | OAuth client credentials               | `<client>:<secret>`   |


## Examples

### Basic connection: HTTP Streaming (HTTP+SSE) HTTP Connection

```bash
http4k-mcp-desktop --url http://localhost:3001/mcp
...or
http4k-mcp-desktop --url http://localhost:3001/mcp --transport http-stream
```

### Basic connection: HTTP Non-streaming (HTTP) + Cursor

```bash
http4k-mcp-desktop --url http://localhost:3001/mcp --transport http-nonstream
```

### Basic SSE Connection (legacy MCP standard)

```bash
http4k-mcp-desktop --url http://localhost:3001/sse --transport sse
```

### HTTP Streaming with auto-discovered (Protected Resource) OAuth Authentication.

```bash
http4k-mcp-desktop --url http://localhost:3001/mcp --oauthClientCredentials client:secret
```

### JSON-RPC with API Key Auth w

```bash
http4k-mcp-desktop --transport jsonrpc --url http://localhost:3001/jsonrpc --apiKey your-api-key
```

### WebSocket with Bearer Token and Reconnect

```bash
http4k-mcp-desktop --transport websocket --url ws://localhost:3001/ws --bearerToken your-token --reconnectDelay 5
```

## Integration with AI Applications

This client is specifically designed to work with desktop AI applications that use StdIO for communication. It handles:

1. Reading input from the AI application via stdin
2. Sending that input to the MCP server
3. Receiving responses from the server
4. Passing those responses back to the application via stdout

This enables seamless connections between desktop AI applications like Claude and remote MCP servers.

### Configuring Claude Desktop

To configure Claude Desktop to use the http4k MCP Desktop Client, you'll need to create a JSON configuration file. Note that if you're on mac and installed the
app via Brew, it will already be on your path. Here's how to set it up:

1. Create a `config.json` file with the following structure:

```json
{
    "command": "/path/to/http4k-mcp-desktop",
    "args": [
        "--url",
        "http://your-mcp-server:port/mcp"
    ],
    "env": {}
}
```

2. Adjust the parameters as needed:
    - Update the path to where you've installed the http4k-mcp-desktop binary. For brew users it's already on your path so just use `http4k-mcp-desktop`
    - Set the correct URL and protocol options for your MCP server (see examples)
    - Add any authentication options required (see examples)

3. In the Claude Desktop application settings, specify the path to your `config.json` file.

#### Example Configurations

**Basic MCP Server Connection:**

```json
{
    "command": "/usr/local/bin/http4k-mcp-desktop",
    "args": [
        "--url",
        "http://localhost:3001/sse"
    ],
    "env": {}
}
```

**With API Key Authentication:**

```json
{
    "command": "/usr/local/bin/http4k-mcp-desktop",
    "args": [
        "--url",
        "http://localhost:3001/sse",
        "--apiKey",
        "your-api-key"
    ],
    "env": {}
}
```

**With OAuth Authentication:**

```json
{
    "command": "/usr/local/bin/http4k-mcp-desktop",
    "args": [
        "--url",
        "http://localhost:3001/sse",
        "--oauthTokenUrl",
        "http://localhost:3001/token",
        "--oauthClientCredentials",
        "client:secret"
    ],
    "env": {}
}
```

## Learn More

- [Model Context Protocol](https://modelcontextprotocol.io/) - Learn about the MCP specification
- [http4k MCP SDK](https://mcp.http4k.org) - Explore the http4k MCP server implementation

## FAQ

- **Q:** I'm using Cursor but I get an error message when installing my MCP connection 
- **A:** Cursor users should currently use HTTP Non-streaming mode (`--transport http-nonstream`)

## License

This project is licensed under the [http4k Commercial License](https://www.http4k.org/commercial-license/), which is totally free for non-commercial, non-profit
and research use.
