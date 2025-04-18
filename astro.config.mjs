// @ts-check
import { defineConfig } from 'astro/config';

import tailwindcss from '@tailwindcss/vite';
import mdx from '@astrojs/mdx';

// https://astro.build/config
export default defineConfig({
  site: 'https://martinklepsch.org',
  output: 'static',
  prefetch: {
    defaultStrategy: 'hover'
  },
  markdown: {
    shikiConfig: { theme: 'github-dark' }
  },
  vite: {
    plugins: [tailwindcss()]
  },
  experimental: {
    svg: true,
  },
  integrations: [mdx()]
});