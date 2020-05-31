SHELL = /bin/bash -o pipefail
.PHONY: update-frontmatter site

update-frontmatter:
	# bootleg -d -e '(glob "content/posts/*.markdown")' | bb -I --stream frontmatter.clj
	ls content/posts/*.markdown | bb -i frontmatter.clj
	ls content/posts/*.md | bb -i frontmatter.clj

site:
	boot build-to-site-dir
	npx prettier --write _site/**.html
