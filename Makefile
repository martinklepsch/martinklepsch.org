SHELL = /bin/bash -o pipefail
.PHONY: update-frontmatter site

repl:
	bb-nrepl -cp bb-src

update-frontmatter:
	bb -cp bb-src -m mkl.frontmatter

site:
	bb -cp bb-src -m mkl.view
	bb -cp bb-src -m mkl.atom
	cp -r resources/public/* _site/
	npx prettier --write _site/**.html _site/posts/*.html
