import { defineCollection, z } from 'astro:content';
import { glob, file } from 'astro/loaders'; // Not available with legacy API


const posts = defineCollection({
  loader: glob({ pattern: ['*.md'], base: 'src/content/posts' }),

  schema: z.object({
    title: z.string(),
    'date-published': z.date(),
    uuid: z.string(),
    slug: z.string(),
    hidden: z.boolean().default(false),
    type: z.string().default('post'),
    'bluesky-post-id': z.string().optional()
  })
});

export const collections = {
  posts,
};
