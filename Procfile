serve: http-server -c-1 -p 8000 _site/
build: find content bb-src -type f | entr bb -cp bb-src -m mkl.view
assets: find resources/public -type f | entr cp -r resources/public/* _site/
tailwind: npx tailwindcss -w -i stylesheets/martinklepschorg-v4.css -o _site/stylesheets/martinklepschorg-v4.css
