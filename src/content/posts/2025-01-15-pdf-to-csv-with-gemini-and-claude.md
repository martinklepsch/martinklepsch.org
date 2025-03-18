---
title: Converting a 700 Page PDF to Excel
type: post
og-image: /images/selfies/10.jpg
date-published: 2025-01-15T20:20:00Z
uuid: 0a5c60c6-a252-4075-a9d1-23fc2ccf402e
slug: pdf-to-csv-with-gemini-and-claude
---

The other day, my flatmate mentioned that a colleague had a 700 page PDF with a bunch of tables that would be nice to have in a spreadsheet. 

With the understanding that those problems are a thing of the past and poor impulse control, we went ahead and started playing with Gemini Advanced and other LLMs trying to extract the data. This blogpost details some of the steps that were necessary to do that reliably.

## Model Choice

While I’ve recently been toying with Gemini Flash 2.0 it didn’t take long to bump against its limits. No matter how many things I called out in the prompt, I couldn’t get it to put empty cells into the CSV as an empty string `""`. Instead, it would simply omit the cell, making every cell slide left and corrupting the entire CSV.

For fun I even tried adding more vertical cell separation by quickly drawing in some black lines but that didn’t help much. Not that it would’ve been a feasible approach anyways.

GPT 4o didn’t do much better either.

In the end I tried Claude and was surprised by just how much better it was. Even with a reduced prompt that didn’t list out the columns it got things right fairly reliably. While my verification isn’t perfect it looks like only a few rows got messed up. 

The full prompt I used:

```md
Extract the tabular data on this page as csv. Respond with only the content of the csv file without any additional text.

Ignore any linebreaks in the table cells. Make sure to quote every cell value.
When encountering empty cells, provide an empty string as value.

You must extract ALL the information available in the entire document.
```

## 700 pages, one at a time

The great thing about extracting tables is that each page is independent, making it much easier to verify your approach at a more granular level. `pdfseparate` made it trivial to split up the PDF:

```
pdfseparate netzausbauplan-2024\ _kurz.pdf pages/%03d.pdf
```

After that, I used [Simon Willison](https://simonwillison.net/)’s excellent [`llm`](https://github.com/simonw/llm) command line tool and wrote a small script that processes a range of pages. 

```
llm -m claude-3-5-sonnet-latest -o temperature 0 -a pages/123.pdf "$(cat prompt.md)"
```

Setting the temperature to 0 wasn’t something that was necessary from my testing, but it seemed appropriate to ask for as-deterministic-as-possible model behavior, given the nature of the task.


## Anthropic Batches API

Now processing all 700 pages is the kind of workload where you get in the range of 2.5M input tokens and 1.4M output tokens, *amounting to about $30*. Perhaps cheap for what it does **but we are cheaper**. 

Doing the math on what it would cost to process the entire PDF I learned about [Anthropic’s Batches API](https://docs.anthropic.com/en/docs/build-with-claude/message-batches#pricing). A variable-latency API that is meant for workloads that don’t require an immediate response. 

Now the `llm` CLI tool does not support the Batches API. But that didn’t stop us. I copied my existing script and some links to Claude’s API docs into Cursor and within 20 minutes I had a script with the same command line API, but using the Batches API. 

Was this necessary? Probably not. Was it fun to make Cursor rewrite my code to use a different API and programming language? **Definitely.**

## Who puts 700 pages full of tables in a PDF? 

Good question. [The PDF](https://downloads.ctfassets.net/xytfb1vrn7of/2upzYS0EhiuuAU4yOYGKwi/18b3903c9a72da15bf914ebb70a52e33/netzausbauplan-2024.pdf) contains a list of planned and ongoing grid development projects across all of Germany. By law (§ 14d Energiewirtschaftsgesetz), all grid operators with more than 100.000 customers are required to publish these plans.

The law seemingly did not state anything about the format in which those plans should be published. So, the publishers, Netze BW GmbH / EnBw, decided: PDF it is!

For those who might have use for this data, you can download it here: [Google Sheet Netzbauplan](https://ggl.link/netzbauplan).

## Update 2024-03-18

I've also run a small experiment with [Mistral's latest OCR model](https://mistral.ai/news/mistral-ocr) after reading [Simon Willison's blog about it](https://simonwillison.net/2025/Mar/7/mistral-ocr/). **Results were not very impressive unfortunately:**

- Several words were garbled, "Projektkategorie" turned into "Projektbiologie", "Baubeginn" into "Boubejims"
- I found these interesting because the PDF does have selectable text so these errors were unexpected

Here are permalinks to [input](/attachments/netzbauplan-page-123.pdf) and [output](/attachments/mistral-ocr-netzbauplan-page-123.html).