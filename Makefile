SHELL = /bin/bash -o pipefail
.PHONY: update-frontmatter site

repl:
	bb-nrepl -cp bb-src

update-frontmatter:
	bb -cp bb-src -m mkl.frontmatter

siten:
	bb -cp bb-str -m mkl.view

site:
	boot build-to-site-dir
	npx prettier --write _site/**.html
