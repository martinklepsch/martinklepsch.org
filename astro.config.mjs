// @ts-check
import { defineConfig } from 'astro/config';

import tailwindcss from '@tailwindcss/vite';
import mdx from '@astrojs/mdx';

// https://astro.build/config
export default defineConfig({
  site: 'https://martinklepsch.org',
  output: 'static',
  markdown: {
    shikiConfig: { theme: 'github-dark' }
  },
  vite: {
    plugins: [tailwindcss()]
  },

  integrations: [mdx()]
});