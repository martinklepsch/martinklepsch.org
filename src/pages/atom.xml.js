import rss from '@astrojs/rss';
import { getCollection } from 'astro:content';
import sanitizeHtml from 'sanitize-html';
import MarkdownIt from 'markdown-it';
const parser = new MarkdownIt();

export async function GET(context) {

  const posts = await getCollection('posts');

  return rss({
    title: 'Martin Klepsch',
    // `<description>` field in output xml
    description: 'A personal blog on everything tickling my curiosity',
    // Pull in your project "site" from the endpoint context
    // https://docs.astro.build/en/reference/api-reference/#site
    site: context.site,
    // Array of `<item>`s in output xml
    // See "Generating items" section for examples using content collections and glob imports
    items: posts.sort((a, b) => b.data['date-published'] - a.data['date-published']).map((post) => ({
      title: post.data.title,
      pubDate: post.data['date-published'],
      description: post.data.description,
      link: `${context.site}/posts/${post.data.slug}`,
      content: sanitizeHtml(parser.render(post.body), {
        allowedTags: sanitizeHtml.defaults.allowedTags.concat(['img'])
      }),
    })),
  });
}
