# http4k MCP Desktop Client

[![http4k Logo](http4k-mcp.png)](https://mcp.http4k.org)

A command-line desktop client for connecting to MCP (Model Context Protocol) servers via standard input/output (StdIO). This client is designed to work
seamlessly with AI assistants and applications that communicate through StdIO, such as Claude and other desktop AI applications.

## Overview

The http4k MCP Desktop Client serves as a proxy that enables communication between desktop applications and MCP servers. While it works with any MCP-compliant
server, it's specially optimized for servers built using the [http4k MCP SDK](https://mcp.http4k.org).

## Features

- Multiple remote transport options: SSE (Server-Sent Events), JSON-RPC and WebSocket. 
- Various authentication methods: API Key, Bearer Token, Basic Auth, and OAuth
- Customizable reconnection strategy
- Simple StdIO interface for easy integration with desktop applications when using natively compiled Kotlin apps.

### Remote Protocol Comparison

At time of writing, the only [MCP Standard](https://spec.modelcontextprotocol.io/) remote protocol is SSE. http4k 
has implemented other standard transports into the http4k-mcp-desktop, as these will be more apprpropriate for distributed/hosted MCP servers.

| Protocol   | Standard/Extension | State      | Default server path | Description |
|------------|-------------------|------------|-------------------|-------------|
| SSE        | Standard MCP      | Stateful   | `http://host/sse` | Server-Sent Events, part of HTML5 spec, ideal for streaming data from server to client |
| WebSocket  | Protocol Extension| Stateful   | `http://host/ws`  | Full-duplex communication protocol, maintains persistent connection |
| JSON-RPC   | Protocol Extension| Stateless  | `http://host/jsonrpc` | Remote Procedure Call protocol encoded in JSON, request/response model |

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
http4k-mcp-desktop --url http://localhost:3001/sse [OPTIONS]
```

## Command Line Options

| Option             | Description                                                                      | Default |
|--------------------|----------------------------------------------------------------------------------|---------|
| `--transport`      | MCP transport mode: `sse` (streaming), `jsonrpc` (non-streaming), or `websocket` | `sse`   |
| `--url`            | URL of the MCP server to connect to (required)                                   | N/A     |
| `--reconnectDelay` | Reconnect delay in seconds if disconnected                                       | 0       |
| `--version`        | Version number                                                                   | 0       |

### Authentication Options

At time of writing, there are no [MCP Standard](https://spec.modelcontextprotocol.io/) authorisation mechanisms. http4k 
has implemented some standard HTTP mechanisms into the http4k-mcp-desktop.

| Option                     | Description                            | Format                |
|----------------------------|----------------------------------------|-----------------------|
| `--apiKey`                 | API key for server authentication      | String                |
| `--apiKeyHeader`           | Custom header name for API key         | `X-Api-key` (default) |
| `--bearerToken`            | Bearer token for server authentication | String                |
| `--basicAuth`              | Basic authentication credentials       | `<user>:<password>`   |
| `--oauthTokenUrl`          | OAuth token endpoint URL               | URL                   |
| `--oauthScopes`            | OAuth scopes to request                | Comma-separated list  |
| `--oauthClientCredentials` | OAuth client credentials               | `<client>:<secret>`   |

## Examples

### Basic SSE Connection

```bash
http4k-mcp-desktop --url http://localhost:3001/sse
```

### JSON-RPC with API Key Auth

```bash
http4k-mcp-desktop --transport jsonrpc --url http://localhost:3001/jsonrpc --apiKey your-api-key
```

### WebSocket with Bearer Token and Reconnect

```bash
http4k-mcp-desktop --transport websocket --url ws://localhost:3001/ws --bearerToken your-token --reconnectDelay 5
```

### OAuth Authentication

```bash
http4k-mcp-desktop --url http://localhost:3001/sse --oauthTokenUrl http://localhost:3001/token --oauthClientCredentials client:secret
```

## Integration with AI Applications

This client is specifically designed to work with desktop AI applications that use StdIO for communication. It handles:

1. Reading input from the AI application via stdin
2. Sending that input to the MCP server
3. Receiving responses from the server
4. Passing those responses back to the application via stdout

This enables seamless connections between desktop AI applications like Claude and remote MCP servers.

### Configuring Claude Desktop

To configure Claude Desktop to use the http4k MCP Desktop Client, you'll need to create a JSON configuration file. Note that if you're on mac and installed the app via Brew, it will already be on your path. Here's how to set it up:

1. Create a `config.json` file with the following structure:

```json
{
  "command": "/path/to/http4k-mcp-desktop",
  "args": [
    "--url", "http://your-mcp-server:port/sse",
    "--transport", "sse"
  ],
  "env": {}
}
```

2. Adjust the parameters as needed:
    - Update the path to where you've installed the http4k-mcp-desktop binary. For brew users it's already on your path so just use `http4k-mcp-dekstop`
    - Set the correct URL and protocol options for your MCP server (see examples)
    - Add any authentication options required (see examples)

3. In the Claude Desktop application settings, specify the path to your `config.json` file.

#### Example Configurations

**Basic MCP Server Connection:**
```json
{
  "command": "/usr/local/bin/http4k-mcp-desktop",
  "args": [
    "--url", "http://localhost:3001/sse"
  ],
  "env": {}
}
```

**With API Key Authentication:**
```json
{
  "command": "/usr/local/bin/http4k-mcp-desktop",
  "args": [
    "--url", "http://localhost:3001/sse",
    "--apiKey", "your-api-key"
  ],
  "env": {}
}
```

**With OAuth Authentication:**
```json
{
  "command": "/usr/local/bin/http4k-mcp-desktop",
  "args": [
    "--url", "http://localhost:3001/sse",
    "--oauthTokenUrl", "http://localhost:3001/token",
    "--oauthClientCredentials", "client:secret"
  ],
  "env": {}
}
```

## Learn More

- [Model Context Protocol](https://modelcontextprotocol.io/) - Learn about the MCP specification
- [http4k MCP SDK](https://mcp.http4k.org) - Explore the http4k MCP server implementation

## License

This project is licensed under the [http4k Commercial License](https://www.http4k.org/commercial-license/), which is totally free for non-commercial, non-profit and research use.
