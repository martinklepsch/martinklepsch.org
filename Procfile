serve: cd _site; python -m SimpleHTTPServer
build: find content bb-src -type f | entr bb -cp bb-src -m mkl.view
assets: find resources/public -type f | entr cp -r resources/public/* _site/
