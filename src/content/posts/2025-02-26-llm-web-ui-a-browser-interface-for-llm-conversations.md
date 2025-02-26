---
type: post
date-published: 2025-02-26
title: "llm-web-ui: A Browser Interface for LLM Conversations"
uuid: 8f2a9c14-d5e7-4b3c-9a8f-e7d6c2f5a3b1
og-image: /images/selfies/5.jpg
slug: llm-web-ui-browser-interface-llm-conversations
---

After spending considerable time with Simon Willison's excellent [`llm` CLI tool](https://llm.datasette.io/), I found myself wanting a more visual way to browse through my conversation history. The CLI is great for quick interactions, but when you're working with multiple conversations or trying to find that one brilliant response from last week, scrolling through terminal output isn't ideal.

That's why I built [`llm-web-ui`](https://github.com/martinklepsch/llm-web-ui) - a simple web interface that makes browsing your LLM conversations much more pleasant.

## What is llm-web-ui?

`llm-web-ui` is a lightweight web interface that reads the conversation logs created by the `llm` CLI tool and presents them in a browser-friendly format. It's designed to complement the CLI experience, not replace it.

With a single command, you can launch a local web server that gives you:

```bash
npx llm-web-ui "$(llm logs path)"
```

## Key Features

- **Conversation History**: Browse through all your past conversations in a clean, organized interface
- **Search Functionality**: Quickly find specific prompts or responses
- **Model Filtering**: Filter conversations by the model you used
- **Token Usage Stats**: See detailed information about token usage for each conversation
- **Multi-turn Navigation**: Easily follow the flow of complex, multi-turn conversations

## Why I Built This

The `llm` CLI tool is fantastic for quick interactions with language models, but I found myself missing some observability features that would help me:

1. **Track my usage patterns**: Which models am I using most frequently?
2. **Find previous solutions**: "I know I solved this problem last month, but what was that prompt again?"
3. **Analyze token efficiency**: Am I writing prompts that use tokens efficiently?

Rather than requesting these features be added to the core `llm` tool (which is purposefully focused on being a lightweight CLI), I decided to build a complementary tool that addresses these specific needs.

## How It Works

The tool is remarkably simple - it reads the SQLite database that `llm` uses to store conversations and presents that data through a React-based web interface. There's no need to set up a separate database or sync mechanism; it works directly with your existing `llm` data.

This means you can continue using the `llm` CLI as your primary interface and only launch the web UI when you need to do some deeper analysis or browsing of your conversation history.

## Getting Started

Installation is straightforward:

```bash
# Using npx (no installation required)
npx llm-web-ui "$(llm logs path)"

# Or install globally
npm install -g llm-web-ui
llm-web-ui "$(llm logs path)"
```

The web interface will open automatically in your default browser.

## What's Next

This is just the beginning for `llm-web-ui`. Some features I'm considering for future releases:

- Conversation tagging for better organization
- Prompt templates library
- Comparative token usage analysis across models
- Export functionality for sharing conversations

If you're using Simon's `llm` tool and want a better way to browse your conversation history, give [`llm-web-ui`](https://github.com/martinklepsch/llm-web-ui) a try and let me know what you think!

Feedback and contributions are always welcome.
