SHELL = /bin/bash -o pipefail
.PHONY: update-frontmatter site

repl-server:
	bb-nrepl -cp bb-src

update-frontmatter:
	bb -cp bb-src -m mkl.frontmatter

site:
	boot build-to-site-dir
	npx prettier --write _site/**.html
