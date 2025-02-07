## High-Level Site Specification

### 1. **Content Sources**
- **Markdown files** in `/content/posts` with frontmatter metadata (title, dates, UUID, permalink, social images)
- Image assets stored in `/resources/public/images`

### 2. Core Features
- **Static HTML generation** of:
  - Chronological blog post listings
  - Individual post pages
  - Atom feed (`/atom.xml`)
  - Archive pages
- **Automatic metadata handling**:
  - UUID generation for posts
  - Permalink creation
  - Featured image selection

### 3. Key Functionalities
- **Content Processing Pipeline**:
  1. Load Markdown files
  2. Extract/enrich frontmatter
  3. Convert Markdown to HTML
  4. Apply templates with consistent header/footer
  5. Output to `/_site` directory

### 4. Presentation Layer
- **Consistent Layout**:
  - Header with site title + Bluesky link
  - Footer with social links
  - Responsive design with Tailwind CSS (`martinklepschorg-v4.css`)
- **Content Styling**:
  - Prose-optimized typography
  - Syntax highlighting
  - Interactive elements (Bluesky comment integration)

### 5. Build Process
- **Static Site Generator** characteristics:
  - No client-side JS except for Bluesky comments
  - Automated Atom feed generation
  - Date-based post sorting
- **Deployment**:
  - Designed for static hosting
  - No server-side rendering

Key migration targets for Astro would be preserving the content structure, metadata handling, and presentation patterns while converting from Clojure/Hiccup to Astro components.

Citations:
[1] https://ppl-ai-file-upload.s3.amazonaws.com/web/direct-files/10472526/fee3f2f4-d29e-4e9d-b208-2d4b0420c498/paste.txt

---
Answer from Perplexity: pplx.ai/share
